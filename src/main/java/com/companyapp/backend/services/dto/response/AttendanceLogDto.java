package com.companyapp.backend.services.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class AttendanceLogDto {
    private UUID id;
    private UUID shiftAssignmentId;
    private UUID userId;
    private String userName;

    // Používáme Instant pro absolutní čas na časové ose (UTC z terminálu)
    private Instant clockInTime;
    private Instant clockOutTime;

    // Např. "APPROVED", "PENDING_REVIEW", "NEEDS_APPROVAL"
    private String status;

    // Pokud nadřízený zamítl přesčas, zde se vrací vysvětlení
    private String reviewNote;
}
