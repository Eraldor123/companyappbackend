package com.companyapp.backend.services;

import com.companyapp.backend.services.dto.response.AuditLogDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditLogService {

    // Původní metoda bez filtrů
    Page<AuditLogDto> getAllLogs(Pageable pageable);

    // NOVÉ: Přidaná metoda s filtry, kterou teď volá Controller
    Page<AuditLogDto> getAllLogsFiltered(Pageable pageable, String search, String module);

    void logAction(String action, String entityName, String entityId, String details);
}