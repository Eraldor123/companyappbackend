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
        // Pro zpětnou kompatibilitu volá původní metodu bez filtrů
        Page<AuditLog> logs = auditLogRepository.findAll(pageable);
        return logs.map(this::mapToDto);
    }

    /**
     * NOVÉ: Metoda pro filtrovaný a stránkovaný výpis logů.
     */
    @Transactional(readOnly = true)
    public Page<AuditLogDto> getAllLogsFiltered(Pageable pageable, String search, String module) {
        Page<AuditLog> logs;

        boolean hasSearch = search != null && !search.trim().isEmpty();
        boolean hasModule = module != null && !module.trim().isEmpty();

        if (hasSearch || hasModule) {
            // Ošetření chyby bytea: posíláme prázdný string "" místo null
            String searchParam = hasSearch ? search.toLowerCase() : "";
            String moduleParam = hasModule ? module : "";

            logs = auditLogRepository.findFilteredLogs(searchParam, moduleParam, pageable);
        } else {
            logs = auditLogRepository.findAll(pageable);
        }

        return logs.map(this::mapToDto);
    }

    @Async("auditTaskExecutor")
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(String action, String entityName, String entityId, String details) {
        String currentUser = "System";
        try {
            // VYLEPŠENÍ: Robustnější zjištění uživatele (počítá i s CustomUserDetails)
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
                Object principal = auth.getPrincipal();
                if (principal instanceof UserDetails) {
                    currentUser = ((UserDetails) principal).getUsername(); // Vytáhne bezpečně e-mail
                } else {
                    currentUser = auth.getName();
                }
            }
        } catch (Exception e) {
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