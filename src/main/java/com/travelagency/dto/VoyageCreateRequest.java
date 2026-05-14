package com.travelagency.dto;

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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class VoyageCreateRequest {
    @NotBlank
    private String titre;
    private String description;
    @NotBlank
    private String destination;
    @NotBlank
    private String paysDestination;
    @NotNull
    private LocalDate dateDepart;
    @NotNull
    private LocalDate dateRetour;
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal prixParPersonne;
    @Min(1)
    private int nombrePlacesTotal;
    private String imageUrl;
    private String categorie;
    private Long hotelId;
    private Long volAllerId;
    private Long volRetourId;
}
