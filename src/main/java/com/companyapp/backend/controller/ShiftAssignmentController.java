package com.companyapp.backend.controller;

import com.companyapp.backend.services.ShiftAssignmentService;
import com.companyapp.backend.services.dto.response.ShiftAssignmentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shift-assignments")
@RequiredArgsConstructor
public class ShiftAssignmentController {

    private final ShiftAssignmentService shiftAssignmentService;

    @PostMapping
    public ResponseEntity<ShiftAssignmentDto> assignShift(
            @RequestParam("shiftId") UUID shiftId, // PŘIDÁNO: "shiftId"
            @RequestParam("userId") UUID userId) { // PŘIDÁNO: "userId"
        ShiftAssignmentDto assignment = shiftAssignmentService.assignShift(shiftId, userId);
        return new ResponseEntity<>(assignment, HttpStatus.CREATED);
    }

    // --- OPRAVENÁ METODA PRO MAZÁNÍ ---
    // Teď to bere parametry shiftId a userId, přesně jak to posílá React
    @DeleteMapping
    public ResponseEntity<Void> removeAssignment(
            @RequestParam("shiftId") UUID shiftId, // PŘIDÁNO: "shiftId"
            @RequestParam("userId") UUID userId) { // PŘIDÁNO: "userId"
        shiftAssignmentService.removeAssignmentByShiftAndUser(shiftId, userId);
        return ResponseEntity.noContent().build();
    }

    // Tuhle původní metodu si tu můžeš nechat pro smazání podle ID záznamu,
    // pokud bys ji někdy potřeboval, ale změnili jsme cestu, aby nekolidovala.
    @DeleteMapping("/id/{id}")
    public ResponseEntity<Void> removeAssignmentById(@PathVariable("id") UUID id) { // PŘIDÁNO: "id"
        shiftAssignmentService.removeAssignment(id);
        return ResponseEntity.noContent().build();
    }
}