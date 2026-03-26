package com.companyapp.backend.controller;

import com.companyapp.backend.services.ShiftService;
import com.companyapp.backend.services.dto.request.ShiftUpdateRequest; // Tohle DTO si hned vytvoříme
import com.companyapp.backend.services.dto.response.ShiftDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shifts")
@RequiredArgsConstructor
public class ShiftController {

    private final ShiftService shiftService;

    // Tohle je ta metoda, která ti teď na frontendu chybí a hází 500
    @PutMapping("/{id}")
    public ResponseEntity<ShiftDto> updateShift(
            @PathVariable UUID id,
            @RequestBody ShiftUpdateRequest updateRequest) {
        return ResponseEntity.ok(shiftService.updateShift(id, updateRequest));
    }
}
