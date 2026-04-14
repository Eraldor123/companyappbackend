package com.companyapp.backend.services.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateCustomShiftRequestDto {
    @NotNull(message = "Stanoviště je povinné.")
    private Integer stationId;

    @NotNull(message = "Počáteční datum je povinné.")
    private LocalDate startDate;

    @NotNull(message = "Koncové datum je povinné.")
    private LocalDate endDate;

    // Změna: Časy už nejsou striktně @NotNull
    private String startTime;
    private String endTime;

    @NotNull(message = "Kapacita je povinná.")
    @Min(value = 1, message = "Kapacita směny musí být alespoň 1.")
    private Integer requiredCapacity;

    // --- NOVÉ BOLEANY PRO OTEVÍRACÍ DOBU ---
    private Boolean useOpeningHours;
    private Boolean hasDopo;
    private Boolean hasOdpo;
    private String description;
}