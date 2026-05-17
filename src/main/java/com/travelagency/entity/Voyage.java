package com.travelagency.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "voyages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Voyage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String destination;

    @Column(nullable = false)
    private String paysDestination;

    @Column(nullable = false)
    private LocalDate dateDepart;

    @Column(nullable = false)
    private LocalDate dateRetour;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prixParPersonne;

    @Column(nullable = false)
    private int nombrePlacesTotal;

    @Column(nullable = false)
    private int nombrePlacesDisponibles;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String imageUrl;
    private String categorie; // COLONIE, ZIARRA, OMRA, HADJ, AUTRE

    @Enumerated(EnumType.STRING)
    private StatutVoyage statut;

    @ManyToOne
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;

    @ManyToOne
    @JoinColumn(name = "vol_aller_id")
    private Vol volAller;

    @ManyToOne
    @JoinColumn(name = "vol_retour_id")
    private Vol volRetour;

    @OneToMany(mappedBy = "voyage")
    private List<Reservation> reservations;

    public boolean hasAvailablePlaces(int requestedPlaces) {
        return nombrePlacesDisponibles >= requestedPlaces;
    }

    public void reduceAvailablePlaces(int count) {
        if (this.nombrePlacesDisponibles < count) {
            throw new IllegalStateException("Pas assez de places disponibles");
        }
        this.nombrePlacesDisponibles -= count;
    }

    public void increaseAvailablePlaces(int count) {
        this.nombrePlacesDisponibles = Math.min(nombrePlacesTotal, nombrePlacesDisponibles + count);
    }

    public int getDureeJours() {
        return (int) (dateRetour.toEpochDay() - dateDepart.toEpochDay());
    }

    public enum StatutVoyage {
        ACTIF, COMPLET, ANNULE, TERMINE
    }
}
