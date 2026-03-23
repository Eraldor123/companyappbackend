package com.companyapp.backend.controller;

import com.companyapp.backend.services.AttendanceProcessingService;
import com.companyapp.backend.services.TerminalAuthenticationService;
import com.companyapp.backend.services.dto.request.TerminalAuthRequestDto;
import com.companyapp.backend.services.dto.response.AttendanceLogDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/terminal")
@RequiredArgsConstructor
public class TerminalAuthController {

    private final TerminalAuthenticationService terminalAuthService;
    private final AttendanceProcessingService attendanceService;

    @PostMapping("/auth")
    public ResponseEntity<Map<String, UUID>> authenticateTerminal(@Valid @RequestBody TerminalAuthRequestDto request) {
        UUID userId = terminalAuthService.authenticateTerminal(request.getPin());
        // Vracíme Mapu pro jednoduchý JSON formát např. {"userId": "123e4567-..."}
        return ResponseEntity.ok(Map.of("userId", userId));
    }

    @PostMapping("/action")
    public ResponseEntity<AttendanceLogDto> handleTerminalAction(@RequestBody Map<String, UUID> request) {
        UUID userId = request.get("userId");
        // Zavolá naši chytrou metodu, která se postará o zbytek
        AttendanceLogDto result = attendanceService.processTerminalAction(userId);
        return ResponseEntity.ok(result);
    }
}
