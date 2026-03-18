package com.companyapp.backend.services.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ShiftGenerationRequestDto {
    @NotNull(message = "Počáteční datum je povinné.")
    private LocalDate startDate;

    @NotNull(message = "Koncové datum je povinné.")
    private LocalDate endDate;

    @NotNull(message = "ID šablony je povinné.")
    private Integer templateId; // ShiftTemplate má ID Integer
}
