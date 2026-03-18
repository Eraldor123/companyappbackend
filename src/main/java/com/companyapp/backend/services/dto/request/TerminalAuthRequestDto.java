package com.companyapp.backend.services.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TerminalAuthRequestDto {
    @NotBlank(message = "Docházkové ID nesmí být prázdné.")
    private String attendanceId;

    @NotBlank(message = "PIN nesmí být prázdný.")
    private String pin;
}
