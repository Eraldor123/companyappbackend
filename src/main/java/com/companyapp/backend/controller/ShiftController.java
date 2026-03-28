package com.companyapp.backend.controller;

import com.companyapp.backend.services.ShiftService;
import com.companyapp.backend.services.dto.request.ShiftUpdateRequest;
import com.companyapp.backend.services.dto.response.ShiftDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // PŘIDÁNO: Pro kontrolu rolí
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shifts")
@RequiredArgsConstructor
public class ShiftController {

    private final ShiftService shiftService;

    /**
     * Aktualizace parametrů směny (čas, kapacita, popisek).
     * Přístupné pouze pro vedení a plánovače.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PLANNER', 'MANAGEMENT')")
    public ResponseEntity<ShiftDto> updateShift(
            @PathVariable UUID id,
            @RequestBody ShiftUpdateRequest updateRequest) {
        return ResponseEntity.ok(shiftService.updateShift(id, updateRequest));
    }

    /**
     * Rozdělení směny na dopolední a odpolední část v 14:00.
     * Přístupné pouze pro vedení a plánovače.
     */
    @PostMapping("/{id}/split")
    @PreAuthorize("hasAnyRole('ADMIN', 'PLANNER', 'MANAGEMENT')")
    public ResponseEntity<Void> splitShift(@PathVariable UUID id) {
        shiftService.splitShift(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Trvalé smazání směny z kalendáře.
     * Přístupné pouze pro vedení a plánovače.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PLANNER', 'MANAGEMENT')")
    public ResponseEntity<Void> deleteShift(@PathVariable UUID id) {
        shiftService.deleteShift(id);
        return ResponseEntity.noContent().build();
    }
}