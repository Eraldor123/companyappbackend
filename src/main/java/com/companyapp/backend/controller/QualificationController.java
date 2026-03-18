package com.companyapp.backend.controller;

import com.companyapp.backend.services.QualificationService;
import com.companyapp.backend.services.dto.request.UpdateQualificationsRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/qualifications")
@RequiredArgsConstructor
public class QualificationController {

    private final QualificationService qualificationService;

    @PutMapping("/users/{userId}")
    public ResponseEntity<Void> updateUserQualifications(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateQualificationsRequestDto request) {

        // Převod Integer (z DTO) na UUID (pro Service interface),
        // ideálně by se měly typy sjednotit přímo v databázi/entitách.
        var qualificationUuids = request.getQualificationIds().stream()
                .map(id -> UUID.nameUUIDFromBytes(id.toString().getBytes()))
                .collect(Collectors.toSet());

        qualificationService.updateUserQualifications(userId, qualificationUuids);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users/{userId}/verify-station/{stationId}")
    public ResponseEntity<Boolean> verifyUserQualificationForStation(
            @PathVariable UUID userId,
            @PathVariable Integer stationId) {
        boolean isQualified = qualificationService.verifyUserQualificationForStation(userId, stationId);
        return ResponseEntity.ok(isQualified);
    }
}
