// src/main/java/com/companyapp/backend/controller/ShiftGenerationController.java
package com.companyapp.backend.controller;

import com.companyapp.backend.services.ShiftGenerationService;
import com.companyapp.backend.services.dto.request.CopyWeekScheduleRequestDto;
import com.companyapp.backend.services.dto.request.ShiftGenerationRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/shift-generation")
@RequiredArgsConstructor
public class ShiftGenerationController {

    private final ShiftGenerationService shiftGenerationService;

    @PostMapping("/from-template")
    public ResponseEntity<List<Object>> generateShiftsFromTemplate(
            @Valid @RequestBody ShiftGenerationRequestDto request) {

        // OPRAVENO: Předáváme rovnou Integer bez UUID převodu
        List<Object> generatedShifts = shiftGenerationService.generateShiftsFromTemplate(
                request.getStartDate(),
                request.getEndDate(),
                request.getTemplateId()
        );
        return new ResponseEntity<>(generatedShifts, HttpStatus.CREATED);
    }

    @PostMapping("/copy-week")
    public ResponseEntity<Void> copyWeekSchedule(
            @Valid @RequestBody CopyWeekScheduleRequestDto request) {
        shiftGenerationService.copyWeekSchedule(
                request.getSourceWeekStart(),
                request.getTargetWeekStart()
        );
        return ResponseEntity.ok().build();
    }


    // Přidej tuto metodu do ShiftGenerationController
    @DeleteMapping("/clear-week")
    public ResponseEntity<Void> clearWeekSchedule(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        shiftGenerationService.clearWeekSchedule(startDate, endDate);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/custom")
    public ResponseEntity<Void> generateCustomShifts(@Valid @RequestBody com.companyapp.backend.services.dto.request.CreateCustomShiftRequestDto request) {
        shiftGenerationService.generateCustomShifts(request);
        return ResponseEntity.ok().build();
    }
}