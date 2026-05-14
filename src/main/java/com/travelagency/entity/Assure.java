package com.travelagency.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "assures")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Assure {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Identité
    @Column(nullable = false)
    private String nom;
    private String prenom;

    // Véhicule
    private String marque;
    private String immatricule;
    private String puissanceFiscale;   // ex: "06CV"
    private String carburant;          // ESSENCE, GAZOLE, HYBRIDE

    // Contrat
    private String numeroPolicce;       // ex: 5102024000176/001
    @Column(precision = 10, scale = 2)
    private BigDecimal montantPrime;

    @Column(nullable = false)
    private LocalDate echeance;         // Date d'échéance

    @Column(nullable = false)
    private LocalDate dateRappel;       // Date de rappel (quelques jours avant)

    private LocalDate periodeGarantieDebut;
    private Integer periodeGarantieAns; // Durée en années

    // Contact
    @Column(nullable = false)
    private String telephone;
    private String telephone2;
    private String email;

    // Statut
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatutAssure statut = StatutAssure.ACTIF;

    private String notes;               // Remarques libres (VENDU, PANNE, etc.)

    @Column(updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    // Notifications envoyées
    @OneToMany(mappedBy = "assure", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<NotificationAssure> notifications;

    public String getNomComplet() {
        return (prenom != null ? prenom + " " : "") + nom;
    }

    public boolean isEcheanceProche() {
        return echeance != null && !echeance.isBefore(LocalDate.now())
            && echeance.isBefore(LocalDate.now().plusDays(35));
    }

    public boolean isExpire() {
        return echeance != null && echeance.isBefore(LocalDate.now());
    }

    public enum StatutAssure {
        ACTIF, EXPIRE, ANNULE, VENDU
    }
}
