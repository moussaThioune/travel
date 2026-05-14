package com.travelagency.dto;

import com.travelagency.entity.Paiement;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaiementDTOs {

    // Requête de paiement client
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Request {
        @NotNull public Long reservationId;
        @NotNull @DecimalMin("0.01") public BigDecimal montant;
        @NotNull public Paiement.ModePaiement modePaiement;
        public String referenceTransaction;
        public String phoneNumber;   // Pour Mobile Money
        public String otpCode;       // OTP vérifié côté client
        public String notes;
    }

    // Réponse paiement
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Response {
        public Long id;
        public String numeroPaiement;
        public Long reservationId;
        public String numeroReservation;
        public String clientNom;
        public String clientEmail;
        public String clientPhone;
        public String voyageTitre;
        public String voyageDestination;
        public BigDecimal montant;
        public Paiement.ModePaiement modePaiement;
        public Paiement.StatutPaiement statut;
        public LocalDateTime datePaiement;
        public String referenceTransaction;
        public String phoneNumber;
        public String validatedAt;
        public String validatedBy;
        public String rejectionReason;
        public String notes;
        public boolean isMobileMoney;
    }

    // Requête validation/rejet admin
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ValidationRequest {
        public String reason; // Pour le rejet
    }

    // Stats paiements pour dashboard
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Stats {
        public long total;
        public long enAttente;
        public long succes;
        public long echec;
        public BigDecimal montantTotal;
        public BigDecimal orangeMoney;
        public BigDecimal wave;
        public BigDecimal freeMoney;
        public BigDecimal carteEtAutres;
    }
}
