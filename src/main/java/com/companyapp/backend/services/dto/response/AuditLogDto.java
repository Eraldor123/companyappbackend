package com.companyapp.backend.services.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class AuditLogDto {
    private UUID id;
    private String action;
    private String entityName;
    private String entityId;
    private String performedBy;
    private LocalDateTime timestamp;
    private String details;
}
