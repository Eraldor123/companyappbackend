package com.companyapp.backend.controller;

import com.companyapp.backend.services.AuditLogService;
import com.companyapp.backend.services.dto.response.AuditLogDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGEMENT')")
    public ResponseEntity<Page<AuditLogDto>> getAllLogs(
            @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String search,   // NOVÉ: Chytá hledaný text z URL
            @RequestParam(required = false) String module    // NOVÉ: Chytá filtr modulu z URL
    ) {
        // Změněno volání na naši novou metodu, která počítá s filtry
        return ResponseEntity.ok(auditLogService.getAllLogsFiltered(pageable, search, module));
    }
}