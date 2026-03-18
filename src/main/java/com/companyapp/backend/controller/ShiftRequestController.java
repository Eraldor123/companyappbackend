package com.companyapp.backend.controller;

import com.companyapp.backend.services.ShiftRequestService;
import com.companyapp.backend.services.dto.request.ShiftCancellationRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/shift-requests")
@RequiredArgsConstructor
public class ShiftRequestController {

    private final ShiftRequestService shiftRequestService;

    @PostMapping("/cancellations")
    public ResponseEntity<Void> submitCancellationRequest(
            @Valid @RequestBody ShiftCancellationRequestDto request) {
        shiftRequestService.submitCancellationRequest(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build(); // 202 Accepted, protože se to teprve bude schvalovat
    }
}
