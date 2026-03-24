package com.companyapp.backend.services.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateStationRequestDto {

    private Integer capacityLimit;
    private Boolean isActive;
    private Boolean needsQualification;
    @NotBlank(message = "Název stanoviště je povinný")
    private String name;

    @NotNull(message = "ID kategorie musí být vyplněno")
    private Integer categoryId;
}
