package com.companyapp.backend.controller;

import com.companyapp.backend.services.AvailabilityService;
import com.companyapp.backend.services.dto.request.MonthlyAvailabilityRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/availabilities")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @GetMapping("/monthly/{userId}/{yearMonthStr}")
    public ResponseEntity<?> getMonthlyAvailability(
            @PathVariable("userId") UUID userId,            // PŘIDÁNO: "userId"
            @PathVariable("yearMonthStr") String yearMonthStr) { // PŘIDÁNO: "yearMonthStr"

        // Převod textu "2026-03" na YearMonth
        YearMonth yearMonth = YearMonth.parse(yearMonthStr);
        return ResponseEntity.ok(availabilityService.getMonthlyAvailability(userId, yearMonth));
    }

    @PostMapping("/monthly")
    public ResponseEntity<Void> submitMonthlyAvailability(
            @Valid @RequestBody MonthlyAvailabilityRequestDto request) {

        // Předáme celý request objekt přímo do service!
        availabilityService.saveMonthlyAvailability(request);
        return ResponseEntity.ok().build();
    }
}