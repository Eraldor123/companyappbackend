package com.companyapp.backend.services;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.companyapp.backend.services.dto.response.AuditLogDto;

public interface AuditLogService {
    void logAction(String action, String entityName, String entityId, String details);
    // ZMĚNĚNO: Vracíme DTO místo entity
    Page<AuditLogDto> getAllLogs(Pageable pageable);
}