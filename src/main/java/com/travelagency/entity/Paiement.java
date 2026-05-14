package com.travelagency.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "paiements")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Paiement {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String numeroPaiement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montant;

    @Enumerated(EnumType.STRING)
    private ModePaiement modePaiement;

    @Enumerated(EnumType.STRING)
    private StatutPaiement statut;

    @Column(nullable = false)
    private LocalDateTime datePaiement;

    private String referenceTransaction;

    // Mobile Money fields
    private String phoneNumber;        // Numéro Mobile Money
    private String validatedAt;        // Date de validation admin
    private String validatedBy;        // Nom de l'admin validateur
    private String rejectionReason;    // Raison du rejet

    private String notes;

    public enum ModePaiement {
        CARTE_BANCAIRE, VIREMENT, CHEQUE, ESPECES, PAYPAL,
        ORANGE_MONEY, WAVE, FREE_MONEY
    }

    public enum StatutPaiement {
        EN_ATTENTE, EN_COURS, SUCCES, ECHEC, REMBOURSE
    }

    public boolean isMobileMoney() {
        return modePaiement == ModePaiement.ORANGE_MONEY
            || modePaiement == ModePaiement.WAVE
            || modePaiement == ModePaiement.FREE_MONEY;
    }
}
