package com.companyapp.backend.services.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ShiftUpdateRequest {

    @NotNull(message = "Počáteční čas nesmí chybět.")
    private LocalDateTime startTime;

    @NotNull(message = "Koncový čas nesmí chybět.")
    private LocalDateTime endTime;

    @Min(value = 1, message = "Kapacita směny musí být alespoň 1.")
    private int requiredCapacity;

    private String description; // Volitelné, může být null
}