package com.companyapp.backend.services.impl;

import com.companyapp.backend.entity.AuditLog;
import com.companyapp.backend.repository.AuditLogRepository;
import com.companyapp.backend.services.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> getAllLogs() {
        // Vrátí všechny logy seřazené podle času od nejnovějšího
        return auditLogRepository.findAll(Sort.by(Sort.Direction.DESC, "timestamp"));
    }

    @Override
    @Transactional
    public void logAction(String action, String entityName, String entityId, String details) {
        String currentUser = "System";
        try {
            // Pokusíme se získat e-mail právě přihlášeného uživatele, který akci provádí
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
            }
        } catch (Exception e) {
            // Pokud se to nepovede (např. systémová automatická akce), zůstane "System"
        }

        AuditLog log = AuditLog.builder()
                .action(action)
                .entityName(entityName)
                .entityId(entityId)
                .performedBy(currentUser)
                .timestamp(LocalDateTime.now())
                .details(details)
                .build();

        auditLogRepository.save(log);
    }
}