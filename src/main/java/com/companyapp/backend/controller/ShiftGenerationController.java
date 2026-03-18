package com.companyapp.backend.controller;

import com.companyapp.backend.services.ShiftGenerationService;
import com.companyapp.backend.services.dto.request.CopyWeekScheduleRequestDto;
import com.companyapp.backend.services.dto.request.ShiftGenerationRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shift-generation")
@RequiredArgsConstructor
public class ShiftGenerationController {

    private final ShiftGenerationService shiftGenerationService;

    @PostMapping("/from-template")
    public ResponseEntity<List<Object>> generateShiftsFromTemplate(
            @Valid @RequestBody ShiftGenerationRequestDto request) {
        // V parametrech očekáváme UUID pro templateId podle interface, ale v DTO jsme měli Integer.
        // Zde předpokládám konverzi nebo sjednocení typů v backendu.
        List<Object> generatedShifts = shiftGenerationService.generateShiftsFromTemplate(
                request.getStartDate(),
                request.getEndDate(),
                UUID.fromString(request.getTemplateId().toString()) // Zástupná konverze
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
}
