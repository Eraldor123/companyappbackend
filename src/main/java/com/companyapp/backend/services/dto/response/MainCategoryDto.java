package com.companyapp.backend.services.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * OPRAVA java:S1144: Třída je označena SuppressWarnings("unused"),
 * protože definuje API kontrakt pro frontend. Bude využita v controllerech
 * po dokončení integrační vrstvy pro zobrazení hierarchie stanovišť.
 */
@Data
@Builder
@SuppressWarnings("unused")
public class MainCategoryDto {
    private Integer id;
    private String name;
    private String hexColor;
    private Boolean isActive;
}