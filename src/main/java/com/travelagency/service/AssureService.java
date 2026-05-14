package com.travelagency.service;

import com.travelagency.dto.AssureDTOs;
import com.travelagency.entity.Assure;
import com.travelagency.entity.NotificationAssure;
import com.travelagency.repository.AssureRepository;
import com.travelagency.repository.NotificationAssureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssureService {

    private final AssureRepository assureRepo;
    private final NotificationAssureRepository notifRepo;
    private final AssureEmailService emailService;
    private final AssureSmsService smsService;

    @Transactional
    public AssureDTOs.Response create(AssureDTOs.Request req) {
        if (req.dateRappel == null && req.echeance != null) {
            req.dateRappel = req.echeance.minusDays(7);
        }
        Assure a = Assure.builder()
            .nom(req.nom).prenom(req.prenom)
            .marque(req.marque).immatricule(req.immatricule)
            .puissanceFiscale(req.puissanceFiscale).carburant(req.carburant)
            .numeroPolicce(req.numeroPolicce).montantPrime(req.montantPrime)
            .echeance(req.echeance).dateRappel(req.dateRappel)
            .periodeGarantieDebut(req.periodeGarantieDebut)
            .periodeGarantieAns(req.periodeGarantieAns)
            .telephone(req.telephone).telephone2(req.telephone2).email(req.email)
            .statut(req.statut != null ? req.statut : Assure.StatutAssure.ACTIF)
            .notes(req.notes)
            .build();
        return toResponse(assureRepo.save(a), true);
    }

    @Transactional
    public AssureDTOs.Response update(Long id, AssureDTOs.Request req) {
        Assure a = findById(id);
        a.setNom(req.nom); a.setPrenom(req.prenom);
        a.setMarque(req.marque); a.setImmatricule(req.immatricule);
        a.setPuissanceFiscale(req.puissanceFiscale); a.setCarburant(req.carburant);
        a.setNumeroPolicce(req.numeroPolicce); a.setMontantPrime(req.montantPrime);
        a.setEcheance(req.echeance);
        a.setDateRappel(req.dateRappel != null ? req.dateRappel : req.echeance.minusDays(7));
        a.setPeriodeGarantieDebut(req.periodeGarantieDebut);
        a.setPeriodeGarantieAns(req.periodeGarantieAns);
        a.setTelephone(req.telephone); a.setTelephone2(req.telephone2); a.setEmail(req.email);
        if (req.statut != null) a.setStatut(req.statut);
        a.setNotes(req.notes);
        a.setUpdatedAt(LocalDateTime.now());
        return toResponse(assureRepo.save(a), true);
    }

    @Transactional
    public void delete(Long id) { assureRepo.deleteById(id); }


    public List<AssureDTOs.Response> getAll() {
        return assureRepo.findAllByOrderByCreatedAtDesc().stream().map(a -> toResponse(a, false)).collect(Collectors.toList());
    }

    public AssureDTOs.Response getById(Long id) {
        return toResponse(findById(id), true);
    }

    public List<AssureDTOs.Response> search(String q) {
        return assureRepo.search(q).stream().map(a -> toResponse(a, false)).collect(Collectors.toList());
    }

    public List<AssureDTOs.Response> getEcheancesProches(int jours) {
        return assureRepo.findEcheancesProches(LocalDate.now(), LocalDate.now().plusDays(jours))
            .stream().map(a -> toResponse(a, false)).collect(Collectors.toList());
    }

    public List<AssureDTOs.Response> getExpires() {
        return assureRepo.findExpires(LocalDate.now())
            .stream().map(a -> toResponse(a, false)).collect(Collectors.toList());
    }

    public AssureDTOs.Stats getStats() {
        int year = LocalDate.now().getYear();
        List<Object[]> parMois = assureRepo.countByMoisEcheance(year);
        String[] moisLabels = {"","Jan","Fév","Mar","Avr","Mai","Jun","Jul","Aoû","Sep","Oct","Nov","Déc"};
        List<AssureDTOs.MoisStat> stats = parMois.stream().map(row ->
            AssureDTOs.MoisStat.builder()
                .mois(((Number) row[0]).intValue())
                .moisLabel(moisLabels[((Number) row[0]).intValue()])
                .count(((Number) row[1]).longValue())
                .build()
        ).collect(Collectors.toList());

        return AssureDTOs.Stats.builder()
            .total(assureRepo.count())
            .actifs(assureRepo.countByStatut(Assure.StatutAssure.ACTIF))
            .echeancesProches(assureRepo.findEcheancesProches(LocalDate.now(), LocalDate.now().plusDays(30)).size())
            .expires(assureRepo.findExpires(LocalDate.now()).size())
            .vendus(assureRepo.countByStatut(Assure.StatutAssure.VENDU))
            .parMois(stats)
            .build();
    }

    @Transactional
    public String notifierManuellement(Long id, AssureDTOs.NotifManuelleRequest req) {
        Assure a = findById(id);
        int sent = 0;
        if (req.email && a.getEmail() != null) {
            emailService.sendRappelEcheance(a, req.messagePersonnalise);
            saveNotif(a, NotificationAssure.TypeNotif.EMAIL, true, "Rappel manuel envoyé");
            sent++;
        }
        if (req.sms) {
            smsService.sendRappel(a, req.messagePersonnalise);
            saveNotif(a, NotificationAssure.TypeNotif.SMS, true, "SMS manuel envoyé");
            sent++;
        }
        return sent + " notification(s) envoyée(s)";
    }

    @Transactional
    public void envoyerRappelsMensuelsTous() {
        List<Assure> aRappeler = assureRepo.findARappeler(LocalDate.now());
        log.info("📅 Job rappels: {} assurés à notifier", aRappeler.size());
        for (Assure a : aRappeler) {
            try {
                // EMAIL
                if (a.getEmail() != null && !a.getEmail().isBlank()) {
                    emailService.sendRappelEcheance(a, null);
                    saveNotif(a, NotificationAssure.TypeNotif.EMAIL, true, "Rappel mensuel automatique");
                }
                // SMS
                smsService.sendRappel(a, null);
                saveNotif(a, NotificationAssure.TypeNotif.SMS, true, "SMS mensuel automatique");
            } catch (Exception e) {
                log.error("Erreur notification assure {}: {}", a.getId(), e.getMessage());
                saveNotif(a, NotificationAssure.TypeNotif.EMAIL, false, "Erreur: " + e.getMessage());
            }
        }
        log.info("✅ Rappels envoyés: {}", aRappeler.size());
    }

    @Transactional
    public void envoyerRappelsDuJour() {
        List<Assure> aRappeler = assureRepo.findARappeler(LocalDate.now()).stream()
            .filter(a -> a.getDateRappel() != null && a.getDateRappel().isEqual(LocalDate.now()))
            .collect(Collectors.toList());
        log.info("🔔 Rappels du jour: {} assurés", aRappeler.size());
        for (Assure a : aRappeler) {
            try {
                if (a.getEmail() != null && !a.getEmail().isBlank()) {
                    emailService.sendRappelEcheance(a, null);
                    saveNotif(a, NotificationAssure.TypeNotif.EMAIL, true, "Rappel automatique du jour");
                }
                smsService.sendRappel(a, null);
                saveNotif(a, NotificationAssure.TypeNotif.SMS, true, "SMS automatique du jour");
            } catch (Exception e) {
                log.error("Erreur rappel quotidien assuré {}: {}", a.getId(), e.getMessage());
            }
        }
    }

    // ===== HELPERS =====
    public Assure findById(Long id) {
        return assureRepo.findById(id).orElseThrow(() -> new RuntimeException("Assuré non trouvé: " + id));
    }

    private void saveNotif(Assure a, NotificationAssure.TypeNotif type, boolean succes, String msg) {
        notifRepo.save(NotificationAssure.builder()
            .assure(a).type(type).succes(succes).message(msg).build());
    }

    public AssureDTOs.Response toResponse(Assure a, boolean withNotifs) {
        long jours = a.getEcheance() != null
            ? ChronoUnit.DAYS.between(LocalDate.now(), a.getEcheance()) : 0;

        List<AssureDTOs.NotifResponse> notifs = null;
        if (withNotifs && a.getId() != null) {
            notifs = notifRepo.findByAssureIdOrderByEnvoyeAtDesc(a.getId()).stream()
                .map(n -> AssureDTOs.NotifResponse.builder()
                    .id(n.getId()).type(n.getType()).succes(n.isSucces())
                    .message(n.getMessage()).envoyeAt(n.getEnvoyeAt()).erreur(n.getErreur())
                    .build())
                .collect(Collectors.toList());
        }

        return AssureDTOs.Response.builder()
            .id(a.getId()).nom(a.getNom()).prenom(a.getPrenom())
            .nomComplet(a.getNomComplet())
            .marque(a.getMarque()).immatricule(a.getImmatricule())
            .puissanceFiscale(a.getPuissanceFiscale()).carburant(a.getCarburant())
            .numeroPolicce(a.getNumeroPolicce()).montantPrime(a.getMontantPrime())
            .echeance(a.getEcheance()).dateRappel(a.getDateRappel())
            .periodeGarantieDebut(a.getPeriodeGarantieDebut())
            .periodeGarantieAns(a.getPeriodeGarantieAns())
            .telephone(a.getTelephone()).telephone2(a.getTelephone2()).email(a.getEmail())
            .statut(a.getStatut()).notes(a.getNotes())
            .echeanceProche(a.isEcheanceProche()).expire(a.isExpire())
            .joursRestants(jours).createdAt(a.getCreatedAt())
            .notifications(notifs)
            .build();
    }
}

