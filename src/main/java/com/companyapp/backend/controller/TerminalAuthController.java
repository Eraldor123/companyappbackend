package com.companyapp.backend.controller;

import com.companyapp.backend.services.TerminalAuthenticationService;
import com.companyapp.backend.services.dto.request.TerminalAuthRequestDto;
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

    @PostMapping("/auth")
    public ResponseEntity<Map<String, UUID>> authenticateTerminal(@Valid @RequestBody TerminalAuthRequestDto request) {
        UUID userId = terminalAuthService.authenticateTerminal(request.getAttendanceId(), request.getPin());
        // Vracíme Mapu pro jednoduchý JSON formát např. {"userId": "123e4567-..."}
        return ResponseEntity.ok(Map.of("userId", userId));
    }
}
