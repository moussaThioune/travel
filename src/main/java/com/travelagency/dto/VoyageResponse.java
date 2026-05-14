package com.travelagency.dto;

import com.travelagency.entity.Voyage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

// ==================== AUTH DTOs ====================

// ==================== VOYAGE DTOs ====================

// ==================== VOYAGE RESPONSE ====================

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class VoyageResponse {
    private Long id;
    private String titre;
    private String description;
    private String destination;
    private String paysDestination;
    private LocalDate dateDepart;
    private LocalDate dateRetour;
    private BigDecimal prixParPersonne;
    private int nombrePlacesTotal;
    private int nombrePlacesDisponibles;
    private String imageUrl;
    private String categorie;
    private Voyage.StatutVoyage statut;
    private int dureeJours;
    private HotelDTO hotel;
    private VolDTO volAller;
    private VolDTO volRetour;
}

// ==================== RESERVATION DTOs ====================

// ==================== PAIEMENT DTOs ====================

