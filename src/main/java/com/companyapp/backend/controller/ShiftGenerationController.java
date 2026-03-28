// src/main/java/com/companyapp/backend/controller/ShiftGenerationController.java
package com.companyapp.backend.controller;

import com.companyapp.backend.services.ShiftGenerationService;
import com.companyapp.backend.services.dto.request.CopyWeekScheduleRequestDto;
import com.companyapp.backend.services.dto.request.ShiftGenerationRequestDto;
import com.companyapp.backend.services.dto.request.CreateCustomShiftRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // PŘIDÁNO
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/shift-generation")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'PLANNER', 'MANAGEMENT')") // OCHRANA CELÉ TŘÍDY
public class ShiftGenerationController {

    private final ShiftGenerationService shiftGenerationService;

    /**
     * Generování směn na základě předdefinované šablony.
     */
    @PostMapping("/from-template")
    public ResponseEntity<List<Object>> generateShiftsFromTemplate(
            @Valid @RequestBody ShiftGenerationRequestDto request) {

        List<Object> generatedShifts = shiftGenerationService.generateShiftsFromTemplate(
                request.getStartDate(),
                request.getEndDate(),
                request.getTemplateId()
        );
        return new ResponseEntity<>(generatedShifts, HttpStatus.CREATED);
    }

    /**
     * Kopírování celého týdenního rozpisu do jiného týdne.
     */
    @PostMapping("/copy-week")
    public ResponseEntity<Void> copyWeekSchedule(
            @Valid @RequestBody CopyWeekScheduleRequestDto request) {
        shiftGenerationService.copyWeekSchedule(
                request.getSourceWeekStart(),
                request.getTargetWeekStart()
        );
        return ResponseEntity.ok().build();
    }

    /**
     * Hromadné smazání všech směn v zadaném časovém rozmezí.
     */
    @DeleteMapping("/clear-week")
    public ResponseEntity<Void> clearWeekSchedule(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        shiftGenerationService.clearWeekSchedule(startDate, endDate);
        return ResponseEntity.noContent().build();
    }

    /**
     * Generování vlastních směn bez použití šablony.
     */
    @PostMapping("/custom")
    public ResponseEntity<Void> generateCustomShifts(
            @Valid @RequestBody CreateCustomShiftRequestDto request) {
        shiftGenerationService.generateCustomShifts(request);
        return ResponseEntity.ok().build();
    }
}