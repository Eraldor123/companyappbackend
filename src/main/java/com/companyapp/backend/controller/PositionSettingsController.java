package com.companyapp.backend.controller;

import com.companyapp.backend.entity.MainCategory;
import com.companyapp.backend.entity.Station;
import com.companyapp.backend.repository.MainCategoryRepository;
import com.companyapp.backend.repository.StationRepository;
import com.companyapp.backend.services.PositionSettingsService;
import com.companyapp.backend.services.dto.request.CreateCategoryRequestDto;
import com.companyapp.backend.services.dto.request.CreateStationRequestDto;
import com.companyapp.backend.services.dto.response.PositionHierarchyDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/position-settings")
@RequiredArgsConstructor
public class PositionSettingsController {

    private final PositionSettingsService positionSettingsService;
    private final MainCategoryRepository categoryRepository;
    private final StationRepository stationRepository;

    @GetMapping("/hierarchy")
    public ResponseEntity<PositionHierarchyDto> getHierarchy() {
        return ResponseEntity.ok(positionSettingsService.getFullHierarchy());
    }

    @PostMapping("/categories")
    public ResponseEntity<?> createCategory(@Valid @RequestBody CreateCategoryRequestDto request) {
        MainCategory category = new MainCategory();
        category.setName(request.getName());
        category.setHexColor(request.getHexColor());
        category.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 1);
        category.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        categoryRepository.save(category);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/stations")
    public ResponseEntity<?> createStation(@Valid @RequestBody CreateStationRequestDto request) {
        // Najdeme kategorii v DB podle ID z requestu
        MainCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Kategorie nenalezena"));

        Station station = new Station();
        station.setName(request.getName());
        station.setCategory(category);
        station.setCapacityLimit(request.getCapacityLimit() != null ? request.getCapacityLimit() : 1);

        // Důležité: Ukládáme true/false přímo z requestu
        station.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        station.setNeedsQualification(request.getNeedsQualification() != null ? request.getNeedsQualification() : false);

        stationRepository.save(station);
        return ResponseEntity.ok().build();
    }
}