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

    // ODEBRÁNO @NotBlank
    private String startTime;
    private String endTime;

    private String startTime2;
    private String endTime2;

    @NotNull
    private Integer workersNeeded;
    private Boolean isActive;
    private Integer sortOrder;

    // PŘIDÁNO:
    private Boolean useOpeningHours;
    private Boolean hasDopo;
    private Boolean hasOdpo;
}