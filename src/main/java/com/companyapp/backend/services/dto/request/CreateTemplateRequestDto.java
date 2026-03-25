package com.companyapp.backend.services.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateTemplateRequestDto {
    @NotNull
    private Integer stationId;

    @NotBlank
    private String name;

    @NotBlank
    private String startTime;

    @NotBlank
    private String endTime;

    private String startTime2;

    private String endTime2;

    @NotNull
    private Integer workersNeeded;

    // --- PŘIDÁNO ---
    private Boolean isActive;
}
