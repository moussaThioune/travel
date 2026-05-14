package com.travelagency.dto;

import com.travelagency.entity.Reservation;
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
class ReservationResponse {
    private Long id;
    private String numeroReservation;
    private ClientDTO client;
    private VoyageResponse voyage;
    private int nombrePersonnes;
    private BigDecimal prixTotal;
    private Reservation.StatutReservation statut;
    private LocalDateTime dateReservation;
    private String notes;
    private BigDecimal montantPaye;
    private BigDecimal montantRestant;
}
