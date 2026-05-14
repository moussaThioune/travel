package com.travelagency.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ReservationRequest {
    @NotNull
    private Long voyageId;
    @Min(1)
    @Max(20)
    private int nombrePersonnes;
    private String notes;
}
