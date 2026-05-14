package com.travelagency.dto;

import com.travelagency.entity.Voyage;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class VoyageDTOs {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank public String titre;
        public String description;
        @NotBlank public String destination;
        @NotBlank public String paysDestination;
        @NotNull public LocalDate dateDepart;
        @NotNull public LocalDate dateRetour;
        @NotNull @DecimalMin("0.01") public BigDecimal prixParPersonne;
        @Min(1) public int nombrePlacesTotal;
        public String imageUrl;
        public String categorie;
        public Long hotelId;
        public Long volAllerId;
        public Long volRetourId;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Response {
        public Long id;
        public String titre;
        public String description;
        public String destination;
        public String paysDestination;
        public LocalDate dateDepart;
        public LocalDate dateRetour;
        public BigDecimal prixParPersonne;
        public int nombrePlacesTotal;
        public int nombrePlacesDisponibles;
        public String imageUrl;
        public String categorie;
        public Voyage.StatutVoyage statut;
        public int dureeJours;
        public HotelSummary hotel;
        public VolSummary volAller;
        public VolSummary volRetour;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class HotelSummary {
        public Long id;
        public String nom;
        public String ville;
        public String pays;
        public int etoiles;
        public String imageUrl;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class VolSummary {
        public Long id;
        public String numeroVol;
        public String compagnie;
        public String villeDepart;
        public String villeArrivee;
        public LocalDateTime dateDepart;
        public LocalDateTime dateArrivee;
    }
}
