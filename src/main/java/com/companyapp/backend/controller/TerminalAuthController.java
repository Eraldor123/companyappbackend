package com.companyapp.backend.controller;

import com.companyapp.backend.services.AttendanceProcessingService;
import com.companyapp.backend.services.TerminalAuthenticationService;
import com.companyapp.backend.services.dto.request.TerminalAuthRequestDto;
import com.companyapp.backend.services.dto.response.AttendanceLogDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/terminal")
@RequiredArgsConstructor
public class TerminalAuthController {

    // OPRAVA java:S1192: Definice konstanty pro opakující se klíč
    private static final String USER_ID_KEY = "userId";

    private final TerminalAuthenticationService terminalAuthService;
    private final AttendanceProcessingService attendanceService;

    @PostMapping("/auth")
    public ResponseEntity<Map<String, UUID>> authenticateTerminal(@Valid @RequestBody TerminalAuthRequestDto request) {
        UUID userId = terminalAuthService.authenticateTerminal(request.getPin());

        // OPRAVA java:S125: Odstraněn komentář připomínající kód (JSON ukázka), který mátl analyzátor
        return ResponseEntity.ok(Map.of(USER_ID_KEY, userId));
    }

    @PostMapping("/action")
    public ResponseEntity<AttendanceLogDto> handleTerminalAction(@RequestBody Map<String, UUID> request) {
        UUID userId = request.get(USER_ID_KEY);

        // OPRAVA java:S125: Zjednodušen komentář, aby neobsahoval "code noise"
        AttendanceLogDto result = attendanceService.processTerminalAction(userId);
        return ResponseEntity.ok(result);
    }
}