package com.companyapp.backend.services.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
public class ShiftDto {
    private UUID id;
    private Integer stationId;
    private String stationName;
    private Integer templateId;
    private String templateName;
    private LocalDate shiftDate;
    private ZonedDateTime startTime;
    private ZonedDateTime endTime;
    private Integer requiredCapacity;
}
