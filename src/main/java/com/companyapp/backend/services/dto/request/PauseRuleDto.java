package com.companyapp.backend.services.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PauseRuleDto {
    private BigDecimal triggerHours;
    private Integer pauseMinutes;
}
