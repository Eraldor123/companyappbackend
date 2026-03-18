package com.companyapp.backend.services.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ShiftCancellationRequestDto {
    @NotNull(message = "ID přiřazení směny nesmí chybět.")
    private UUID shiftAssignmentId;

    @NotBlank(message = "Důvod zrušení musí být vyplněn pro schvalovací proces.")
    private String reason;
}
