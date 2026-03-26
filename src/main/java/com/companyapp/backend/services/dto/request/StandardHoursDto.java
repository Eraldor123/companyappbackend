package com.companyapp.backend.services.dto.request;

import lombok.Data;

@Data
public class StandardHoursDto {
    private String weekDopoStart;
    private String weekDopoEnd;
    private String weekOdpoStart;
    private String weekOdpoEnd;
    private Boolean weekendSame;
    private String weekendDopoStart;
    private String weekendDopoEnd;
    private String weekendOdpoStart;
    private String weekendOdpoEnd;
}
