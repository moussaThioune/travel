package com.travelagency.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "demandes_vol")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DemandeVol {

    public enum Statut {
        EN_ATTENTE,       // client submitted, waiting for admin response
        REPONSE_ENVOYEE,  // admin sent pricing
        ACCEPTE,          // client accepted pricing
        REJETE,           // client rejected pricing
        VALIDEE,          // admin validated
        PAYE,             // payment done
        BILLET_EMIS       // ticket issued
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String numeroDemande;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Search criteria
    @Column(nullable = false)
    private String origine;

    @Column(nullable = false)
    private String destination;

    @Column(nullable = false)
    private LocalDate dateDepart;

    private LocalDate dateRetour;

    @Column(nullable = false)
    private String typeVoyage; // aller-simple / aller-retour

    @Column(nullable = false)
    private int nbAdultes;

    private int nbEnfants;
    private int nbBebes;

    @Column(nullable = false)
    private String classeVoyage;

    private boolean volsDirects;
    private boolean aeroportsProximiteOrigine;
    private boolean aeroportsProximiteDestination;

    @Column(length = 1000)
    private String notesClient;

    // Admin response
    private String compagnieAerienne;
    private Double prixParPersonne;
    private Double prixTotal;
    private String dureeVol;
    private String escales;
    private String numeroBillet;

    @Column(length = 2000)
    private String notesAdmin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Statut statut;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public int totalPassagers() {
        return nbAdultes + nbEnfants + nbBebes;
    }
}
