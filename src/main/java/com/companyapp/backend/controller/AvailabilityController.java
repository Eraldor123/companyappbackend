package com.companyapp.backend.controller;

import com.companyapp.backend.services.AvailabilityService;
import com.companyapp.backend.services.dto.request.MonthlyAvailabilityRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // PŘIDÁNO
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/availabilities")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @GetMapping("/monthly/{userId}/{yearMonthStr}")
    // OCHRANA 1: Můžeš vidět jen svůj kalendář, ledaže jsi ADMIN/MANAGER/PLANNER
    @PreAuthorize("#userId == authentication.principal.id or hasAnyRole('ADMIN', 'MANAGEMENT', 'PLANNER')")
    public ResponseEntity<?> getMonthlyAvailability(
            @PathVariable("userId") UUID userId,
            @PathVariable("yearMonthStr") String yearMonthStr) {

        YearMonth yearMonth = YearMonth.parse(yearMonthStr);
        return ResponseEntity.ok(availabilityService.getMonthlyAvailability(userId, yearMonth));
    }

    @PostMapping("/monthly")
    // OCHRANA 2: Můžeš upravit jen svůj kalendář, ledaže jsi ADMIN/MANAGER
    @PreAuthorize("#request.userId == authentication.principal.id or hasAnyRole('ADMIN', 'MANAGEMENT')")
    public ResponseEntity<Void> submitMonthlyAvailability(
            @Valid @RequestBody MonthlyAvailabilityRequestDto request) {

        availabilityService.saveMonthlyAvailability(request);
        return ResponseEntity.ok().build();
    }
}