package com.companyapp.backend.services.impl;

import com.companyapp.backend.entity.AuditLog;
import com.companyapp.backend.repository.AuditLogRepository;
import com.companyapp.backend.services.AuditLogService;
import com.companyapp.backend.services.dto.response.AuditLogDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async; // PŘIDÁNO
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDto> getAllLogs(Pageable pageable) {
        Page<AuditLog> logs = auditLogRepository.findAll(pageable);

        // FÁZE 3: DTO mapping (předchází úniku entit do kontroleru)
        return logs.map(log -> AuditLogDto.builder()
                .id(log.getId())
                .action(log.getAction())
                .entityName(log.getEntityName())
                .entityId(log.getEntityId())
                .performedBy(log.getPerformedBy())
                .timestamp(log.getTimestamp())
                .details(log.getDetails())
                .build());
    }

    /**
     * FÁZE 3: Asynchronní logování akcí.
     * @Async("auditTaskExecutor") zajistí, že zápis do DB proběhne v pozadí.
     * Propagation.REQUIRES_NEW zaručí, že se log zapíše, i když hlavní akce (např. uložení směny) selže.
     */
    @Async("auditTaskExecutor")
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(String action, String entityName, String entityId, String details) {
        String currentUser = "System";
        try {
            // Díky DelegatingSecurityContextAsyncTaskExecutor v AsyncConfig zde uvidíme e-mail uživatele
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
            }
        } catch (Exception ignored) {}

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