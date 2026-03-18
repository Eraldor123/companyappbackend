package com.companyapp.backend.services.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Data
public class MonthlyAvailabilityRequestDto {
    @NotNull(message = "ID uživatele je povinné.")
    private UUID userId;

    @NotNull(message = "Měsíc a rok musí být specifikován.")
    private YearMonth month;

    @NotNull(message = "Seznam dostupných dnů nesmí chybět.")
    private List<LocalDate> availableDays;
}
