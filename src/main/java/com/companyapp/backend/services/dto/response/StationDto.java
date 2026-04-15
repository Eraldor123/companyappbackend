package com.companyapp.backend.services.dto.response;

import lombok.Builder;
import lombok.Data;
@SuppressWarnings("unused")
@Data
@Builder
public class StationDto {
    private Integer id;
    private String name;
    private Integer categoryId;
    private String categoryName;
    private Integer capacityLimit;
    private Integer reqQualificationId;
    private String reqQualificationName;
    private Boolean isActive;
}
