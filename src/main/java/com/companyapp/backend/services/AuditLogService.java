package com.companyapp.backend.services;
import com.companyapp.backend.entity.AuditLog;

import java.util.List;
public interface AuditLogService {
    void logAction(String action, String entityName, String entityId, String details);

    List<AuditLog> getAllLogs();
}
