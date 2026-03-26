package com.companyapp.backend.services.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class DailyHoursDto {
    private LocalDate date;
    private String dopoStart;
    private String dopoEnd;
    private String odpoStart;
    private String odpoEnd;
    private boolean isSeasonal;
}