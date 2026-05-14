package com.travelagency.dto;

import com.travelagency.entity.Reservation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ReservationDTOs {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Request {
        @NotNull public Long voyageId;
        @Min(1) @Max(20) public int nombrePersonnes;
        public String notes;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Response {
        public Long id;
        public String numeroReservation;
        public ClientSummary client;
        public VoyageDTOs.Response voyage;
        public int nombrePersonnes;
        public BigDecimal prixTotal;
        public Reservation.StatutReservation statut;
        public LocalDateTime dateReservation;
        public String notes;
        public BigDecimal montantPaye;
        public BigDecimal montantRestant;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ClientSummary {
        public Long id;
        public String firstName;
        public String lastName;
        public String email;
        public String phone;
    }

}
