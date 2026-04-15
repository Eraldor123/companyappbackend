package com.companyapp.backend.services.impl;

import com.companyapp.backend.entity.AuditLog;
import com.companyapp.backend.repository.AuditLogRepository;
import com.companyapp.backend.services.AuditLogService;
import com.companyapp.backend.services.dto.response.AuditLogDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // PŘIDÁNO
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j // PŘIDÁNO pro čistší logování
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDto> getAllLogs(Pageable pageable) {
        Page<AuditLog> logs = auditLogRepository.findAll(pageable);
        return logs.map(this::mapToDto);
    }

    /**
     * OPRAVA: Pokud bean 'auditTaskExecutor' neexistuje, změň na @Async bez parametru
     * nebo prověř svůj AsyncConfig.
     */
    @Async("auditTaskExecutor")
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(String action, String entityName, String entityId, String details) {
        String currentUser = "System";
        try {
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
            }
        } catch (Exception e) {
            // OPRAVA: Vyřešení prázdného catch bloku.
            // Logujeme pro debug účely, ale nebráníme uložení logu (Fallback na "System").
            log.debug("Nepodařilo se získat uživatele ze SecurityContextu: {}", e.getMessage());
        }

        AuditLog auditLog = AuditLog.builder()
                .action(action)
                .entityName(entityName)
                .entityId(entityId)
                .performedBy(currentUser)
                .timestamp(LocalDateTime.now())
                .details(details)
                .build();

        auditLogRepository.save(auditLog);
    }

    private AuditLogDto mapToDto(AuditLog logEntity) {
        return AuditLogDto.builder()
                .id(logEntity.getId())
                .action(logEntity.getAction())
                .entityName(logEntity.getEntityName())
                .entityId(logEntity.getEntityId())
                .performedBy(logEntity.getPerformedBy())
                .timestamp(logEntity.getTimestamp())
                .details(logEntity.getDetails())
                .build();
    }
}