package com.companyapp.backend.controller;

import com.companyapp.backend.services.QualificationService;
import com.companyapp.backend.services.dto.request.UpdateQualificationsRequestDto;
import com.companyapp.backend.services.dto.response.EmployeeQualificationDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/qualifications")
@RequiredArgsConstructor
public class QualificationController {

    private final QualificationService qualificationService;

    @GetMapping("/employees")
    public ResponseEntity<List<EmployeeQualificationDto>> getAllEmployeesWithQualifications() {
        // Změněno na: getAllEmployeesWithStations()
        return ResponseEntity.ok(qualificationService.getAllEmployeesWithStations());
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<Void> updateUserQualifications(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateQualificationsRequestDto request) {
        // Změněno na: updateUserStations()
        qualificationService.updateUserStations(userId, request.getQualificationIds());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users/{userId}/verify-station/{stationId}")
    public ResponseEntity<Boolean> verifyUserQualificationForStation(
            @PathVariable UUID userId,
            @PathVariable Integer stationId) {
        // Změněno na: isUserQualifiedForStation()
        return ResponseEntity.ok(qualificationService.isUserQualifiedForStation(userId, stationId));
    }
}