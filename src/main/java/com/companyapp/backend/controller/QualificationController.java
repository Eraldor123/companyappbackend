package com.companyapp.backend.controller;

import com.companyapp.backend.services.QualificationService;
import com.companyapp.backend.services.dto.request.UpdateQualificationsRequestDto;
import com.companyapp.backend.services.dto.response.EmployeeQualificationDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/qualifications")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGEMENT')")
public class QualificationController {

    private final QualificationService qualificationService;

    @GetMapping("/employees")
    public ResponseEntity<Page<EmployeeQualificationDto>> getAllEmployeesWithQualifications(
            @org.springframework.data.web.PageableDefault(size = 15) Pageable pageable) {
        // Frontend nyní může volat /api/v1/qualifications/employees?page=0&size=15
        return ResponseEntity.ok(qualificationService.getAllEmployeesWithStations(pageable));
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