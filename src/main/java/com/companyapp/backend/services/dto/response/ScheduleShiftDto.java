package com.companyapp.backend.services.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
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

    @Builder.Default // OCHRANA PROTI NULL - prázdná směna pošle [] a ne null
    private List<AssignedUserDto> assignedUsers = new ArrayList<>();
}