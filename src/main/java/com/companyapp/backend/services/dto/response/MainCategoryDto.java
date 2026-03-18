package com.companyapp.backend.services.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MainCategoryDto {
    private Integer id;
    private String name;
    private String hexColor;
    private Boolean isActive;
}
