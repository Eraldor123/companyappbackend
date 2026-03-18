package com.companyapp.backend.controller;

import com.companyapp.backend.services.ShiftAssignmentService;
import com.companyapp.backend.services.dto.response.ShiftAssignmentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shift-assignments")
@RequiredArgsConstructor
public class ShiftAssignmentController {

    private final ShiftAssignmentService shiftAssignmentService;

    @PostMapping
    public ResponseEntity<ShiftAssignmentDto> assignShift(
            @RequestParam UUID shiftId,
            @RequestParam UUID userId) {
        ShiftAssignmentDto assignment = shiftAssignmentService.assignShift(shiftId, userId);
        return new ResponseEntity<>(assignment, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeAssignment(@PathVariable UUID id) {
        shiftAssignmentService.removeAssignment(id);
        return ResponseEntity.noContent().build();
    }
}
