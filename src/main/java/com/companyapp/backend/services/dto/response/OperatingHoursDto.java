package com.companyapp.backend.services.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;

@Data
@Builder
public class OperatingHoursDto {
    private LocalTime openTime;
    private LocalTime closeTime;
    private boolean isSeasonalRegime;
    private String regimeName; // Např. "Halloween speciál"
}
