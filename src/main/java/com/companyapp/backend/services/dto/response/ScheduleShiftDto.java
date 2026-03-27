package com.companyapp.backend.services.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ScheduleShiftDto {
    private UUID id;
    private Integer stationId;
    private Integer templateId;
    private LocalDate shiftDate;
    private String startTime;
    private String endTime;
    private Integer requiredCapacity;
    private String description;

    // Tady frontend uvidí, kdo už tam je.
    // Pokud size() < requiredCapacity -> vykreslí červenou
    // Pokud size() == requiredCapacity -> vykreslí zelenou
    private List<AssignedUserDto> assignedUsers;
}