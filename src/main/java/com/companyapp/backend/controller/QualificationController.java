package com.companyapp.backend.controller;

import com.companyapp.backend.services.QualificationService;
import com.companyapp.backend.services.dto.request.UpdateQualificationsRequestDto;
import com.companyapp.backend.services.dto.response.EmployeeQualificationDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/qualifications")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGEMENT', 'PLANNER')")
public class QualificationController {

    private final QualificationService qualificationService;

    @GetMapping("/employees")
    public ResponseEntity<Page<EmployeeQualificationDto>> getAllEmployeesWithQualifications(
            @PageableDefault(size = 15) Pageable pageable,
            @RequestParam(required = false) String search,        // NOVÉ: Volitelný parametr pro vyhledávání
            @RequestParam(required = false) String contractType   // NOVÉ: Volitelný parametr pro filtr úvazku
    ) {
        // Voláme novou metodu v Service vrstvě, která už počítá s filtry
        return ResponseEntity.ok(qualificationService.getAllEmployeesWithStationsFiltered(pageable, search, contractType));
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<Void> updateUserQualifications(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateQualificationsRequestDto request) {
        qualificationService.updateUserStations(userId, request.getQualificationIds());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users/{userId}/verify-station/{stationId}")
    public ResponseEntity<Boolean> verifyUserQualificationForStation(
            @PathVariable UUID userId,
            @PathVariable Integer stationId) {
        return ResponseEntity.ok(qualificationService.isUserQualifiedForStation(userId, stationId));
    }
}