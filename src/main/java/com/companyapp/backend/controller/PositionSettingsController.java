package com.companyapp.backend.controller;
import com.companyapp.backend.repository.ShiftTemplateRepository;
import com.companyapp.backend.services.dto.request.CreateTemplateRequestDto;
import com.companyapp.backend.entity.ShiftTemplate;
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

import java.time.LocalTime;

@RestController
@RequestMapping("/api/v1/position-settings")
@RequiredArgsConstructor
public class PositionSettingsController {

    private final PositionSettingsService positionSettingsService;
    private final MainCategoryRepository categoryRepository;
    private final StationRepository stationRepository;
    private final ShiftTemplateRepository shiftTemplateRepository;
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
    @PostMapping("/templates")
    public ResponseEntity<?> createTemplate(@Valid @RequestBody CreateTemplateRequestDto request) {
        Station station = stationRepository.findById(request.getStationId())
                .orElseThrow(() -> new RuntimeException("Stanoviště nenalezeno"));

        ShiftTemplate template = new ShiftTemplate();
        template.setStation(station);
        template.setName(request.getName());
        template.setStartTime(LocalTime.parse(request.getStartTime()));
        template.setEndTime(LocalTime.parse(request.getEndTime()));
        template.setWorkersNeeded(request.getWorkersNeeded());

        // --- PŘIDÁNO: Pokud přijde i druhá část směny, uložíme ji ---
        if (request.getStartTime2() != null && !request.getStartTime2().isEmpty()) {
            template.setStartTime2(LocalTime.parse(request.getStartTime2()));
        }
        if (request.getEndTime2() != null && !request.getEndTime2().isEmpty()) {
            template.setEndTime2(LocalTime.parse(request.getEndTime2()));
        }

        // --- PŘIDÁNO: Uložení aktivního stavu ---
        template.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        shiftTemplateRepository.save(template);
        return ResponseEntity.ok().build();
    }

    // ==========================================
    // EDITACE A MAZÁNÍ - KATEGORIE
    // ==========================================
    @PutMapping("/categories/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Integer id, @Valid @RequestBody CreateCategoryRequestDto request) {
        MainCategory category = categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Kategorie nenalezena"));
        category.setName(request.getName());
        category.setHexColor(request.getHexColor());
        category.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 1);

        boolean wasActive = category.getIsActive();
        boolean willBeActive = request.getIsActive() != null ? request.getIsActive() : true;
        category.setIsActive(willBeActive);

        // KASKÁDOVÁ DEAKTIVACE: Pokud vypínáme kategorii, vypneme i podřazené prvky
        if (wasActive && !willBeActive) {
            java.util.List<Station> stations = stationRepository.findAll().stream()
                    .filter(s -> s.getCategory().getId().equals(id))
                    .collect(java.util.stream.Collectors.toList());

            for (Station s : stations) {
                s.setIsActive(false);
                stationRepository.save(s);
                java.util.List<ShiftTemplate> templates = shiftTemplateRepository.findByStationId(s.getId());
                for (ShiftTemplate t : templates) {
                    t.setIsActive(false);
                    shiftTemplateRepository.save(t);
                }
            }
        }
        categoryRepository.save(category);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Integer id) {
        // KASKÁDOVÉ SMAZÁNÍ: Nejdřív šablony, pak stanoviště, pak kategorie (Ochrana Foreign Key)
        java.util.List<Station> stations = stationRepository.findAll().stream()
                .filter(s -> s.getCategory().getId().equals(id))
                .collect(java.util.stream.Collectors.toList());

        for (Station s : stations) {
            java.util.List<ShiftTemplate> templates = shiftTemplateRepository.findByStationId(s.getId());
            shiftTemplateRepository.deleteAll(templates);
        }
        stationRepository.deleteAll(stations);
        categoryRepository.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // EDITACE A MAZÁNÍ - STANOVIŠTĚ
    // ==========================================
    @PutMapping("/stations/{id}")
    public ResponseEntity<?> updateStation(@PathVariable Integer id, @Valid @RequestBody CreateStationRequestDto request) {
        Station station = stationRepository.findById(id).orElseThrow(() -> new RuntimeException("Stanoviště nenalezeno"));
        station.setName(request.getName());
        station.setCapacityLimit(request.getCapacityLimit() != null ? request.getCapacityLimit() : 1);
        station.setNeedsQualification(request.getNeedsQualification() != null ? request.getNeedsQualification() : false);

        boolean wasActive = station.getIsActive();
        boolean willBeActive = request.getIsActive() != null ? request.getIsActive() : true;
        station.setIsActive(willBeActive);

        // KASKÁDOVÁ DEAKTIVACE: Pokud vypínáme stanoviště, vypneme i jeho šablony
        if (wasActive && !willBeActive) {
            java.util.List<ShiftTemplate> templates = shiftTemplateRepository.findByStationId(id);
            for (ShiftTemplate t : templates) {
                t.setIsActive(false);
                shiftTemplateRepository.save(t);
            }
        }
        stationRepository.save(station);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/stations/{id}")
    public ResponseEntity<?> deleteStation(@PathVariable Integer id) {
        // KASKÁDOVÉ SMAZÁNÍ: Nejdřív šablony, pak stanoviště
        java.util.List<ShiftTemplate> templates = shiftTemplateRepository.findByStationId(id);
        shiftTemplateRepository.deleteAll(templates);
        stationRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // EDITACE A MAZÁNÍ - ŠABLONY
    // ==========================================
    @PutMapping("/templates/{id}")
    public ResponseEntity<?> updateTemplate(@PathVariable Integer id, @Valid @RequestBody CreateTemplateRequestDto request) {
        ShiftTemplate template = shiftTemplateRepository.findById(id).orElseThrow(() -> new RuntimeException("Šablona nenalezena"));
        template.setName(request.getName());
        template.setWorkersNeeded(request.getWorkersNeeded());
        template.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        template.setStartTime(request.getStartTime() != null ? LocalTime.parse(request.getStartTime()) : null);
        template.setEndTime(request.getEndTime() != null ? LocalTime.parse(request.getEndTime()) : null);

        if (request.getStartTime2() != null && !request.getStartTime2().isEmpty()) {
            template.setStartTime2(LocalTime.parse(request.getStartTime2()));
            template.setEndTime2(LocalTime.parse(request.getEndTime2()));
        } else {
            template.setStartTime2(null);
            template.setEndTime2(null);
        }
        shiftTemplateRepository.save(template);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/templates/{id}")
    public ResponseEntity<?> deleteTemplate(@PathVariable Integer id) {
        shiftTemplateRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}