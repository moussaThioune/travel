package com.travelagency.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class VolDTO {
    private Long id;
    private String numeroVol;
    private String compagnie;
    private String villeDepart;
    private String villeArrivee;
    private java.time.LocalDateTime dateDepart;
    private java.time.LocalDateTime dateArrivee;
    private String classeVol;
}
