package com.travelagency.service;

import com.travelagency.dto.DemandeVolDTOs;
import com.travelagency.entity.DemandeVol;
import com.travelagency.entity.User;
import com.travelagency.repository.DemandeVolRepository;
import com.travelagency.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DemandeVolService {

    private final DemandeVolRepository repo;
    private final UserRepository userRepo;
    private final DemandeVolEmailService emailService;

    // ===== CLIENT =====

    @Transactional
    public DemandeVolDTOs.Response submit(DemandeVolDTOs.Request req) {
        User user = currentUser();

        DemandeVol d = DemandeVol.builder()
                .numeroDemande(generateRef())
                .user(user)
                .origine(req.getOrigine())
                .destination(req.getDestination())
                .dateDepart(req.getDateDepart())
                .dateRetour(req.getDateRetour())
                .typeVoyage(req.getTypeVoyage())
                .nbAdultes(req.getNbAdultes())
                .nbEnfants(req.getNbEnfants())
                .nbBebes(req.getNbBebes())
                .classeVoyage(req.getClasseVoyage())
                .volsDirects(req.isVolsDirects())
                .aeroportsProximiteOrigine(req.isAeroportsProximiteOrigine())
                .aeroportsProximiteDestination(req.isAeroportsProximiteDestination())
                .notesClient(req.getNotesClient())
                .statut(DemandeVol.Statut.EN_ATTENTE)
                .build();

        d = repo.save(d);
        emailService.sendConfirmationClient(d);
        emailService.sendNotificationAdmin(d);
        return DemandeVolDTOs.Response.from(d);
    }

    public List<DemandeVolDTOs.Response> getMesDemandes() {
        User user = currentUser();
        return repo.findByUserOrderByCreatedAtDesc(user).stream()
                .map(DemandeVolDTOs.Response::from).collect(Collectors.toList());
    }

    @Transactional
    public DemandeVolDTOs.Response accepter(Long id) {
        DemandeVol d = getAndCheckOwner(id);
        if (d.getStatut() != DemandeVol.Statut.REPONSE_ENVOYEE) {
            throw new IllegalStateException("La demande n'est pas en attente d'acceptation");
        }
        d.setStatut(DemandeVol.Statut.ACCEPTE);
        d = repo.save(d);
        emailService.sendClientAccepte(d);
        emailService.sendAdminClientAccepte(d);
        return DemandeVolDTOs.Response.from(d);
    }

    @Transactional
    public DemandeVolDTOs.Response rejeter(Long id) {
        DemandeVol d = getAndCheckOwner(id);
        if (d.getStatut() != DemandeVol.Statut.REPONSE_ENVOYEE) {
            throw new IllegalStateException("La demande n'est pas en attente d'acceptation");
        }
        d.setStatut(DemandeVol.Statut.REJETE);
        return DemandeVolDTOs.Response.from(repo.save(d));
    }

    // ===== ADMIN =====

    public List<DemandeVolDTOs.Response> getAll() {
        return repo.findAllByOrderByCreatedAtDesc().stream()
                .map(DemandeVolDTOs.Response::from).collect(Collectors.toList());
    }

    public DemandeVolDTOs.Response getById(Long id) {
        return DemandeVolDTOs.Response.from(repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Demande introuvable")));
    }

    @Transactional
    public DemandeVolDTOs.Response repondre(Long id, DemandeVolDTOs.ReponseAdminRequest req) {
        DemandeVol d = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Demande introuvable"));
        d.setCompagnieAerienne(req.getCompagnieAerienne());
        d.setPrixParPersonne(req.getPrixParPersonne());
        d.setPrixTotal(req.getPrixTotal());
        d.setDureeVol(req.getDureeVol());
        d.setEscales(req.getEscales());
        d.setNotesAdmin(req.getNotesAdmin());
        d.setStatut(DemandeVol.Statut.REPONSE_ENVOYEE);
        d = repo.save(d);
        emailService.sendReponseClient(d);
        return DemandeVolDTOs.Response.from(d);
    }

    @Transactional
    public DemandeVolDTOs.Response valider(Long id) {
        DemandeVol d = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Demande introuvable"));
        if (d.getStatut() != DemandeVol.Statut.ACCEPTE) {
            throw new IllegalStateException("La demande doit être acceptée par le client avant validation");
        }
        d.setStatut(DemandeVol.Statut.VALIDEE);
        d = repo.save(d);
        emailService.sendValidationClient(d);
        return DemandeVolDTOs.Response.from(d);
    }

    @Transactional
    public DemandeVolDTOs.Response marquerPaye(Long id) {
        DemandeVol d = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Demande introuvable"));
        d.setStatut(DemandeVol.Statut.PAYE);
        return DemandeVolDTOs.Response.from(repo.save(d));
    }

    @Transactional
    public DemandeVolDTOs.Response emettreTicket(Long id, DemandeVolDTOs.BilletRequest req) {
        DemandeVol d = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Demande introuvable"));
        d.setNumeroBillet(req.getNumeroBillet());
        if (req.getNotesAdmin() != null) d.setNotesAdmin(req.getNotesAdmin());
        d.setStatut(DemandeVol.Statut.BILLET_EMIS);
        d = repo.save(d);
        emailService.sendBilletClient(d);
        return DemandeVolDTOs.Response.from(d);
    }

    // ===== HELPERS =====

    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }

    private DemandeVol getAndCheckOwner(Long id) {
        User user = currentUser();
        DemandeVol d = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Demande introuvable"));
        if (!d.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Accès non autorisé");
        }
        return d;
    }

    private String generateRef() {
        return "VOL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
