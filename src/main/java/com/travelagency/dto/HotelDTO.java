package com.travelagency.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class HotelDTO {
    private Long id;
    private String nom;
    private String ville;
    private String pays;
    private int etoiles;
    private String imageUrl;
}
