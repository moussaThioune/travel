package com.travelagency.service;

import com.travelagency.entity.Assure;
import com.travelagency.repository.AssureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientImportService {

    private final AssureRepository assureRepo;

    public record ImportResult(int imported, int skipped, int errors, List<String> messages) {}

    /**
     * Importe les assurés depuis un fichier Excel (.xlsx)
     * Colonnes attendues (dans l'ordre) :
     * NOM | PRENOM | MARQUE | IMMATRICULE | PERIODE_GARANTIE | ECHEANCE | DATE_RAPPEL | TELEPHONE | NOTES
     */
    public ImportResult importExcel(MultipartFile file) throws IOException {
        int imported = 0, skipped = 0, errors = 0;
        List<String> messages = new ArrayList<>();

        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            int firstRow = 1; // Ligne 0 = en-têtes

            // Auto-détection si ligne 0 commence par "NOM" (en-tête), sinon démarrer à 0
            Row headerRow = sheet.getRow(0);
            if (headerRow != null) {
                String cell0 = getCellString(headerRow.getCell(0));
                if (!cell0.toUpperCase().contains("NOM")) firstRow = 0;
            }

            for (int i = firstRow; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                // Ignorer les lignes entièrement vides
                String nom = getCellString(row.getCell(0));
                if (nom.isBlank()) { skipped++; continue; }

                try {
                    String prenom      = getCellString(row.getCell(1));
                    String marque      = getCellString(row.getCell(2));
                    String immat       = getCellString(row.getCell(3));
                    String periodeStr  = getCellString(row.getCell(4));
                    String echeanceStr = getCellString(row.getCell(5));
                    String rappelStr   = getCellString(row.getCell(6));
                    String tel         = getCellString(row.getCell(7));
                    String notes       = row.getLastCellNum() > 8 ? getCellString(row.getCell(8)) : "";

                    if (tel.isBlank()) {
                        messages.add("Ligne " + (i+1) + " ignorée (téléphone manquant) : " + nom);
                        skipped++; continue;
                    }

                    LocalDate echeance = parseDate(echeanceStr);
                    LocalDate rappel   = parseDate(rappelStr);
                    if (echeance == null) {
                        messages.add("Ligne " + (i+1) + " ignorée (date invalide) : " + nom + " — " + echeanceStr);
                        skipped++; continue;
                    }
                    if (rappel == null) rappel = echeance.minusDays(7);

                    // Statut auto selon notes
                    Assure.StatutAssure statut = Assure.StatutAssure.ACTIF;
                    String notesLower = notes.toLowerCase();
                    if (notesLower.contains("vendu") || notesLower.contains("vendue")) statut = Assure.StatutAssure.VENDU;
                    else if (echeance.isBefore(LocalDate.now())) statut = Assure.StatutAssure.EXPIRE;

                    // Période de garantie (ex: "3" ou "1" → années)
                    Integer periodeAns = null;
                    try { if (!periodeStr.isBlank()) periodeAns = (int) Double.parseDouble(periodeStr.trim()); } catch (Exception ignored) {}

                    Assure a = Assure.builder()
                        .nom(nom.trim().toUpperCase())
                        .prenom(prenom.trim().isEmpty() ? null : capitalize(prenom.trim()))
                        .marque(marque.trim().isEmpty() ? null : marque.trim().toUpperCase())
                        .immatricule(immat.trim().isEmpty() ? null : immat.trim().toUpperCase())
                        .periodeGarantieAns(periodeAns)
                        .echeance(echeance)
                        .dateRappel(rappel)
                        .telephone(tel.trim().replaceAll("\\s+", " "))
                        .statut(statut)
                        .notes(notes.trim().isEmpty() ? null : notes.trim())
                        .build();

                    assureRepo.save(a);
                    imported++;

                } catch (Exception e) {
                    errors++;
                    messages.add("Ligne " + (i+1) + " erreur : " + e.getMessage());
                    log.warn("Erreur import ligne {}: {}", i+1, e.getMessage());
                }
            }
        }

        messages.add(0, String.format("✅ %d importés | ⚠️ %d ignorés | ❌ %d erreurs", imported, skipped, errors));
        return new ImportResult(imported, skipped, errors, messages);
    }

    private String getCellString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    // Date Excel → String
                    LocalDate d = cell.getLocalDateTimeCellValue().toLocalDate();
                    yield d.toString();
                }
                // Numéro (ex: téléphone stocké en nombre)
                long lv = (long) cell.getNumericCellValue();
                yield String.valueOf(lv);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try { yield cell.getStringCellValue(); }
                catch (Exception e) { yield String.valueOf((long) cell.getNumericCellValue()); }
            }
            default -> "";
        };
    }

    private LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        s = s.trim();
        // Essayer plusieurs formats
        String[] formats = {
            "yyyy-MM-dd", "dd/MM/yyyy", "M/d/yyyy", "d/M/yyyy",
            "MM/dd/yyyy", "dd-MM-yyyy", "d/MM/yyyy", "dd/M/yyyy"
        };
        for (String fmt : formats) {
            try { return LocalDate.parse(s, DateTimeFormatter.ofPattern(fmt)); }
            catch (DateTimeParseException ignored) {}
        }
        return null;
    }

    private String capitalize(String s) {
        if (s.isBlank()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
}
