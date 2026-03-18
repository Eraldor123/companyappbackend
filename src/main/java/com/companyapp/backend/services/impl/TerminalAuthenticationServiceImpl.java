package com.companyapp.backend.services.impl;

import com.companyapp.backend.entity.User;
import com.companyapp.backend.repository.UserRepository;
import com.companyapp.backend.services.TerminalAuthenticationService;
import com.companyapp.backend.services.exception.InvalidPinException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TerminalAuthenticationServiceImpl implements TerminalAuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UUID authenticateTerminal(String attendanceId, String rawPin) {
        // Vyhledáme uživatele podle jeho unikátního docházkového ID
        User user = userRepository.findByAttendanceId(attendanceId)
                .orElseThrow(() -> new InvalidPinException("Zadané Docházkové ID nebo PIN je nesprávný."));

        // Ověření PINu pomocí BCrypt mechanismu (prevence proti plain text úniku)
        if (!passwordEncoder.matches(rawPin, user.getPinHash())) {
            log.warn("Neúspěšný pokus o přihlášení na terminálu pro Docházkové ID: {}", attendanceId);
            throw new InvalidPinException("Zadané Docházkové ID nebo PIN je nesprávný.");
        }

        if (!user.isActive()) {
            throw new InvalidPinException("Tento účet byl deaktivován.");
        }

        log.info("Uživatel s ID {} se úspěšně autorizoval na terminálu.", user.getId());
        return user.getId();
    }
}