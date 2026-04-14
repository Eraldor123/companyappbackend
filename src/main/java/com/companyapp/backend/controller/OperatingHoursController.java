package com.companyapp.backend.controller;

import com.companyapp.backend.services.OperatingHoursService;
import com.companyapp.backend.services.dto.request.PauseRuleDto;
import com.companyapp.backend.services.dto.request.SeasonalRegimeDto;
import com.companyapp.backend.services.dto.request.StandardHoursDto;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // PŘIDANÝ IMPORT
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/operating-hours")
@RequiredArgsConstructor
public class OperatingHoursController {

    private final OperatingHoursService operatingHoursService;

    // STARÝ ENDPOINT PRO ZJIŠTĚNÍ OTEVÍRACÍ DOBY V DANÝ DEN
    @GetMapping
    public ResponseEntity<Object> getOperatingHoursForDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Object operatingHours = operatingHoursService.getOperatingHoursForDate(date);
        return ResponseEntity.ok(operatingHours);
    }

    // =====================================
    // ENDPOINTY PRO ZÁLOŽKU "PROVOZ AREÁLU" (REACT)
    // =====================================

    // 1. Standardní hodiny
    @GetMapping("/standard")
    public ResponseEntity<StandardHoursDto> getStandardHours() {
        return ResponseEntity.ok(operatingHoursService.getStandardHours());
    }

    @PutMapping("/standard")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGEMENT')") // ZABEZPEČENO
    public ResponseEntity<Void> updateStandardHours(@RequestBody StandardHoursDto dto) {
        operatingHoursService.updateStandardHours(dto);
        return ResponseEntity.ok().build();
    }

    // 2. Pauzy
    @GetMapping("/pause-rule")
    public ResponseEntity<PauseRuleDto> getPauseRule() {
        return ResponseEntity.ok(operatingHoursService.getPauseRule());
    }

    @PutMapping("/pause-rule")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGEMENT')") // ZABEZPEČENO
    public ResponseEntity<Void> updatePauseRule(@RequestBody PauseRuleDto dto) {
        operatingHoursService.updatePauseRule(dto);
        return ResponseEntity.ok().build();
    }

    // 3. Sezónní režimy
    @GetMapping("/seasons")
    public ResponseEntity<List<SeasonalRegimeDto>> getAllSeasons() {
        return ResponseEntity.ok(operatingHoursService.getAllSeasons());
    }

    @PostMapping("/seasons")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGEMENT')") // ZABEZPEČENO
    public ResponseEntity<Void> createSeason(@RequestBody SeasonalRegimeDto dto) {
        operatingHoursService.saveSeason(dto);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/seasons/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGEMENT')") // ZABEZPEČENO
    public ResponseEntity<Void> updateSeason(@PathVariable Integer id, @RequestBody SeasonalRegimeDto dto) {
        dto.setId(id);
        operatingHoursService.saveSeason(dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/seasons/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGEMENT')") // ZABEZPEČENO
    public ResponseEntity<Void> deleteSeason(@PathVariable Integer id) {
        operatingHoursService.deleteSeason(id);
        return ResponseEntity.noContent().build();
    }
}