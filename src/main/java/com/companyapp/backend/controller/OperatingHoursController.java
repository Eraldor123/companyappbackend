package com.companyapp.backend.controller;

import com.companyapp.backend.services.OperatingHoursService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/operating-hours")
@RequiredArgsConstructor
public class OperatingHoursController {

    private final OperatingHoursService operatingHoursService;

    @GetMapping
    public ResponseEntity<Object> getOperatingHoursForDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Object operatingHours = operatingHoursService.getOperatingHoursForDate(date);
        return ResponseEntity.ok(operatingHours);
    }
}
