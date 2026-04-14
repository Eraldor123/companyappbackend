package com.companyapp.backend.controller;

import com.companyapp.backend.services.PositionSettingsService;
import com.companyapp.backend.services.dto.request.CreateCategoryRequestDto;
import com.companyapp.backend.services.dto.request.CreateStationRequestDto;
import com.companyapp.backend.services.dto.request.CreateTemplateRequestDto;
import com.companyapp.backend.services.dto.response.PositionHierarchyDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/position-settings")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGEMENT')") // Zabezpečení celé třídy
public class PositionSettingsController {

    private final PositionSettingsService positionSettingsService;

    @GetMapping("/hierarchy")
    public ResponseEntity<PositionHierarchyDto> getHierarchy() {
        return ResponseEntity.ok(positionSettingsService.getFullHierarchy());
    }

    // --- KATEGORIE ---
    @PostMapping("/categories")
    public ResponseEntity<Void> createCategory(@Valid @RequestBody CreateCategoryRequestDto request) {
        positionSettingsService.createCategory(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<Void> updateCategory(@PathVariable Integer id, @Valid @RequestBody CreateCategoryRequestDto request) {
        positionSettingsService.updateCategory(id, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Integer id) {
        positionSettingsService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    // --- STANOVIŠTĚ ---
    @PostMapping("/stations")
    public ResponseEntity<Void> createStation(@Valid @RequestBody CreateStationRequestDto request) {
        positionSettingsService.createStation(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/stations/{id}")
    public ResponseEntity<Void> updateStation(@PathVariable Integer id, @Valid @RequestBody CreateStationRequestDto request) {
        positionSettingsService.updateStation(id, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/stations/{id}")
    public ResponseEntity<Void> deleteStation(@PathVariable Integer id) {
        positionSettingsService.deleteStation(id);
        return ResponseEntity.noContent().build();
    }

    // --- ŠABLONY ---
    @PostMapping("/templates")
    public ResponseEntity<Void> createTemplate(@Valid @RequestBody CreateTemplateRequestDto request) {
        positionSettingsService.createTemplate(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/templates/{id}")
    public ResponseEntity<Void> updateTemplate(@PathVariable Integer id, @Valid @RequestBody CreateTemplateRequestDto request) {
        positionSettingsService.updateTemplate(id, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/templates/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Integer id) {
        positionSettingsService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }
}