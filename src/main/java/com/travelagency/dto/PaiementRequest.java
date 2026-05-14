package com.travelagency.dto;

import com.travelagency.entity.Paiement;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class PaiementRequest {
    @NotNull
    private Long reservationId;
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal montant;
    @NotNull
    private Paiement.ModePaiement modePaiement;
    private String referenceTransaction;
    private String notes;
}
