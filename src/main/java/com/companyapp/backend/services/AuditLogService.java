package com.companyapp.backend.services;

import com.companyapp.backend.services.dto.response.AuditLogDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditLogService {

    Page<AuditLogDto> getAllLogs(Pageable pageable);

    Page<AuditLogDto> getAllLogsFiltered(Pageable pageable, String search, String module);

    // Původní metoda (pro automatické zjištění uživatele)
    void logAction(String action, String entityName, String entityId, String details);

    // NOVÁ PŘETÍŽENÁ METODA: Umožňuje ručně zadat, kdo akci provedl (např. email při loginu)
    void logAction(String action, String entityName, String entityId, String details, String performedBy);
}