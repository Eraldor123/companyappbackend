package com.companyapp.backend.services.dto.request;

import lombok.Data;

@Data
public class SeasonalRegimeDto {
    private Integer id;
    private String name;
    private String startDate;
    private String endDate;
    private String dopoStart;
    private String dopoEnd;
    private String odpoStart;
    private String odpoEnd;
    private Boolean isActive;
}
