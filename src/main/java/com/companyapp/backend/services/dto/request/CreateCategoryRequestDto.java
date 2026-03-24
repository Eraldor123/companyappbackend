package com.companyapp.backend.services.dto.request;

import lombok.Data;

@Data
public class CreateCategoryRequestDto {
    private String name;
    private String hexColor;
    private Integer sortOrder; // PŘIDÁNO
    private Boolean isActive;  // PŘIDÁNO
}
