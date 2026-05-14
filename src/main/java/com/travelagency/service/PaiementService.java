package com.travelagency.service;

import com.travelagency.dto.PaiementDTOs;
import com.travelagency.entity.Paiement;
import com.travelagency.entity.Reservation;
import com.travelagency.entity.User;
import com.travelagency.repository.PaiementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaiementService {

    private final PaiementRepository paiementRepository;
    private final ReservationService reservationService;
    private final EmailService emailService;

    @Transactional
    public PaiementDTOs.Response processPaiement(PaiementDTOs.Request request) {
        Reservation reservation = reservationService.findById(request.reservationId);

        if (reservation.getStatut() == Reservation.StatutReservation.ANNULEE) {
            throw new RuntimeException("Impossible de payer une réservation annulée");
        }
        if (request.montant.compareTo(reservation.getMontantRestant()) > 0) {
            throw new RuntimeException("Montant supérieur au reste dû: " + reservation.getMontantRestant());
        }

        boolean isMobile = isMobileMoney(request.modePaiement);
        // Mobile Money → EN_ATTENTE (nécessite validation admin)
        // Autres méthodes → SUCCES immédiat
        Paiement.StatutPaiement statut = isMobile
            ? Paiement.StatutPaiement.EN_ATTENTE
            : Paiement.StatutPaiement.SUCCES;

        String prefix = switch (request.modePaiement) {
            case ORANGE_MONEY -> "OM-";
            case WAVE -> "WV-";
            case FREE_MONEY -> "FM-";
            case PAYPAL -> "PP-";
            case CARTE_BANCAIRE -> "CB-";
            default -> "PAY-";
        };

        Paiement paiement = Paiement.builder()
                .numeroPaiement(prefix + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .reservation(reservation)
                .montant(request.montant)
                .modePaiement(request.modePaiement)
                .statut(statut)
                .datePaiement(LocalDateTime.now())
                .referenceTransaction(request.referenceTransaction)
                .phoneNumber(request.phoneNumber)
                .notes(request.notes)
                .build();

        paiement = paiementRepository.save(paiement);

        // Si paiement immédiat (carte, virement, PayPal) → MAJ statut réservation
        if (!isMobile) {
            updateReservationAfterPayment(reservation);
            emailService.sendPaiementConfirmation(paiement);
        } else {
            // Mobile Money → email d'attente
            emailService.sendMobileMoneyPending(paiement);
            log.info("Paiement Mobile Money en attente de validation: {} - {} FCFA",
                paiement.getNumeroPaiement(), paiement.getMontant());
        }

        return toResponse(paiement);
    }

    // ===== VALIDATION ADMIN =====
    @Transactional
    public PaiementDTOs.Response validerPaiement(Long id) {
        Paiement paiement = findById(id);
        if (paiement.getStatut() != Paiement.StatutPaiement.EN_ATTENTE) {
            throw new RuntimeException("Ce paiement n'est pas en attente de validation");
        }

        String adminName = getCurrentAdminName();
        paiement.setStatut(Paiement.StatutPaiement.SUCCES);
        paiement.setValidatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        paiement.setValidatedBy(adminName);
        paiementRepository.save(paiement);

        // Mettre à jour la réservation
        updateReservationAfterPayment(paiement.getReservation());

        // Envoyer email de confirmation au client
        emailService.sendPaiementConfirmation(paiement);

        log.info("Paiement {} validé par {}", paiement.getNumeroPaiement(), adminName);
        return toResponse(paiement);
    }

    @Transactional
    public PaiementDTOs.Response rejeterPaiement(Long id, String reason) {
        Paiement paiement = findById(id);
        if (paiement.getStatut() != Paiement.StatutPaiement.EN_ATTENTE) {
            throw new RuntimeException("Ce paiement n'est pas en attente de validation");
        }

        String adminName = getCurrentAdminName();
        paiement.setStatut(Paiement.StatutPaiement.ECHEC);
        paiement.setRejectionReason(reason);
        paiement.setValidatedBy(adminName);
        paiementRepository.save(paiement);

        emailService.sendPaiementRejete(paiement, reason);

        log.info("Paiement {} rejeté par {}: {}", paiement.getNumeroPaiement(), adminName, reason);
        return toResponse(paiement);
    }

    // ===== GETTERS =====
    public List<PaiementDTOs.Response> getByReservation(Long reservationId) {
        return paiementRepository.findByReservationId(reservationId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<PaiementDTOs.Response> getAll() {
        return paiementRepository.findAll().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<PaiementDTOs.Response> getMobileMoneyEnAttente() {
        return paiementRepository.findMobileMoneyEnAttente().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<PaiementDTOs.Response> getAllMobileMoney() {
        return paiementRepository.findAllMobileMoney().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public PaiementDTOs.Stats getStats() {
        return PaiementDTOs.Stats.builder()
            .total(paiementRepository.count())
            .enAttente(paiementRepository.countMobileMoneyEnAttente())
            .succes(paiementRepository.findByStatut(Paiement.StatutPaiement.SUCCES).size())
            .echec(paiementRepository.findByStatut(Paiement.StatutPaiement.ECHEC).size())
            .montantTotal(paiementRepository.sumAllSucces())
            .orangeMoney(paiementRepository.sumByModeAndSucces(Paiement.ModePaiement.ORANGE_MONEY))
            .wave(paiementRepository.sumByModeAndSucces(Paiement.ModePaiement.WAVE))
            .freeMoney(paiementRepository.sumByModeAndSucces(Paiement.ModePaiement.FREE_MONEY))
            .carteEtAutres(paiementRepository.sumByModeAndSucces(Paiement.ModePaiement.CARTE_BANCAIRE)
                .add(paiementRepository.sumByModeAndSucces(Paiement.ModePaiement.PAYPAL))
                .add(paiementRepository.sumByModeAndSucces(Paiement.ModePaiement.VIREMENT)))
            .build();
    }

    // ===== HELPERS =====
    private void updateReservationAfterPayment(Reservation reservation) {
        reservationService.refreshReservation(reservation.getId());
    }

    private boolean isMobileMoney(Paiement.ModePaiement mode) {
        return mode == Paiement.ModePaiement.ORANGE_MONEY
            || mode == Paiement.ModePaiement.WAVE
            || mode == Paiement.ModePaiement.FREE_MONEY;
    }

    private String getCurrentAdminName() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "Administrateur";
        }
    }

    public Paiement findById(Long id) {
        return paiementRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Paiement non trouvé: " + id));
    }

    public PaiementDTOs.Response toResponse(Paiement p) {
        Reservation res = p.getReservation();
        return PaiementDTOs.Response.builder()
                .id(p.getId())
                .numeroPaiement(p.getNumeroPaiement())
                .reservationId(res.getId())
                .numeroReservation(res.getNumeroReservation())
                .clientNom(res.getClient().getFullName())
                .clientEmail(res.getClient().getEmail())
                .clientPhone(res.getClient().getPhone())
                .voyageTitre(res.getVoyage().getTitre())
                .voyageDestination(res.getVoyage().getDestination())
                .montant(p.getMontant())
                .modePaiement(p.getModePaiement())
                .statut(p.getStatut())
                .datePaiement(p.getDatePaiement())
                .referenceTransaction(p.getReferenceTransaction())
                .phoneNumber(p.getPhoneNumber())
                .validatedAt(p.getValidatedAt())
                .validatedBy(p.getValidatedBy())
                .rejectionReason(p.getRejectionReason())
                .notes(p.getNotes())
                .isMobileMoney(p.isMobileMoney())
                .build();
    }
}
