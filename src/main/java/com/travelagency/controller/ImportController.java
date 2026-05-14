package com.travelagency.controller;

import com.travelagency.service.ClientImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
public class ImportController {

    private final ClientImportService importService;

    @PostMapping("/assures/excel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> importAssures(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "Fichier vide"));
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "";
        if (!filename.endsWith(".xlsx") && !filename.endsWith(".xls")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Format requis : .xlsx ou .xls"));
        }
        try {
            ClientImportService.ImportResult result = importService.importExcel(file);
            return ResponseEntity.ok(Map.of(
                "imported", result.imported(),
                "skipped",  result.skipped(),
                "errors",   result.errors(),
                "messages", result.messages()
            ));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Erreur lecture fichier: " + e.getMessage()));
        }
    }
}
