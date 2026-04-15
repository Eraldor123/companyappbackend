package com.companyapp.backend.controller;

import com.companyapp.backend.services.AvailabilityService;
import com.companyapp.backend.services.dto.request.AvailabilityDTO;
import com.companyapp.backend.services.dto.request.MonthlyAvailabilityRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/availabilities")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    /**
     * OPRAVENO: ResponseEntity<?> nahrazeno konkrétním typem List<AvailabilityDTO>.
     * Odstraňuje SonarLint warning a zpřehledňuje API kontrakt.
     */
    @GetMapping("/monthly/{userId}/{yearMonthStr}")
    @PreAuthorize("#userId == authentication.principal.id or hasAnyRole('ADMIN', 'MANAGEMENT', 'PLANNER')")
    public ResponseEntity<List<AvailabilityDTO>> getMonthlyAvailability(
            @PathVariable("userId") UUID userId,
            @PathVariable("yearMonthStr") String yearMonthStr) {

        YearMonth yearMonth = YearMonth.parse(yearMonthStr);
        return ResponseEntity.ok(availabilityService.getMonthlyAvailability(userId, yearMonth));
    }

    @PostMapping("/monthly")
    @PreAuthorize("#request.userId == authentication.principal.id or hasAnyRole('ADMIN', 'MANAGEMENT')")
    public ResponseEntity<Void> submitMonthlyAvailability(
            @Valid @RequestBody MonthlyAvailabilityRequestDto request) {

        availabilityService.saveMonthlyAvailability(request);
        return ResponseEntity.ok().build();
    }
}