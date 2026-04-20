package com.companyapp.backend.services.impl;

import com.companyapp.backend.entity.AuditLog;
import com.companyapp.backend.repository.AuditLogRepository;
import com.companyapp.backend.services.AuditLogService;
import com.companyapp.backend.services.dto.response.AuditLogDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDto> getAllLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable).map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDto> getAllLogsFiltered(Pageable pageable, String search, String module) {
        String searchParam = (search != null) ? search.toLowerCase() : "";
        String moduleParam = (module != null) ? module : "";
        return auditLogRepository.findFilteredLogs(searchParam, moduleParam, pageable).map(this::mapToDto);
    }

    // Automatická verze - využívá novou metodu s hodnotou null pro performer
    @Async("auditTaskExecutor")
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(String action, String entityName, String entityId, String details) {
        this.logAction(action, entityName, entityId, details, null);
    }

    // NOVÁ VERZE: Pokud je performedBy zadán, použije se. Jinak zkusí SecurityContext.
    @Async("auditTaskExecutor")
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(String action, String entityName, String entityId, String details, String performedBy) {
        String user = performedBy;

        if (user == null) {
            user = "System";
            try {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
                    Object principal = auth.getPrincipal();
                    user = (principal instanceof UserDetails) ? ((UserDetails) principal).getUsername() : auth.getName();
                }
            } catch (Exception e) {
                log.debug("Audit: Nepodařilo se získat uživatele z kontextu.");
            }
        }

        AuditLog auditLog = AuditLog.builder()
                .action(action)
                .entityName(entityName)
                .entityId(entityId)
                .performedBy(user)
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