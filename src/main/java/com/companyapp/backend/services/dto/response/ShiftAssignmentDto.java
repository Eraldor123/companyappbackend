package com.companyapp.backend.services.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
public class ShiftAssignmentDto {
    private UUID id;
    private UUID shiftId;
    private UUID userId;

    // Zploštělá data (Flattened data) pro snadné zobrazení v Reactu
    private String userName;
    private String stationName;
    private String categoryColorHex; // Pro barevný štítek v UI

    // Využíváme ZonedDateTime kvůli možným posunům času a letnímu/zimnímu času
    private ZonedDateTime startTime;
    private ZonedDateTime endTime;
}
