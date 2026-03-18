package com.companyapp.backend.controller;

import com.companyapp.backend.services.AvailabilityService;
import com.companyapp.backend.services.dto.request.MonthlyAvailabilityRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/availabilities")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @PostMapping("/monthly")
    public ResponseEntity<Void> submitMonthlyAvailability(
            @Valid @RequestBody MonthlyAvailabilityRequestDto request) {
        availabilityService.submitMonthlyAvailability(
                request.getUserId(),
                request.getMonth(),
                request.getAvailableDays()
        );
        return ResponseEntity.ok().build();
    }
}
