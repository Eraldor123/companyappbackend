package com.companyapp.backend.controller;

import com.companyapp.backend.services.FacilityManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/facilities")
@RequiredArgsConstructor
public class FacilityManagementController {

    private final FacilityManagementService facilityManagementService;

    @PatchMapping("/stations/{stationId}/deactivate")
    public ResponseEntity<Void> deactivateStation(@PathVariable UUID stationId) {
        facilityManagementService.deactivateStation(stationId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/categories/{categoryId}/deactivate")
    public ResponseEntity<Void> deactivateMainCategory(@PathVariable UUID categoryId) {
        facilityManagementService.deactivateMainCategory(categoryId);
        return ResponseEntity.noContent().build();
    }
}
