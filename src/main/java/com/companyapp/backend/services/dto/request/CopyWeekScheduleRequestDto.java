package com.companyapp.backend.services.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CopyWeekScheduleRequestDto {
    @NotNull(message = "Počáteční datum zdrojového týdne je povinné.")
    private LocalDate sourceWeekStart;

    @NotNull(message = "Počáteční datum cílového týdne je povinné.")
    private LocalDate targetWeekStart;
}
