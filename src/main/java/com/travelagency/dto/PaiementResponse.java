package com.travelagency.dto;

import com.travelagency.entity.Paiement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class PaiementResponse {
    private Long id;
    private String numeroPaiement;
    private Long reservationId;
    private String numeroReservation;
    private BigDecimal montant;
    private Paiement.ModePaiement modePaiement;
    private Paiement.StatutPaiement statut;
    private LocalDateTime datePaiement;
    private String referenceTransaction;
}
