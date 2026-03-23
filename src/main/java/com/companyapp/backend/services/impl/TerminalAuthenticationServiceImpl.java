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
    public UUID authenticateTerminal(String rawPin) {
        // Zahashujeme zadaný PIN z terminálu
        String hashedPin = passwordEncoder.encode(rawPin);

        // Najdeme uživatele rovnou podle hashe (v User.java už máš @Column(unique = true))
        User user = userRepository.findByPinAndIsActiveTrue(hashedPin)
                .orElseThrow(() -> {
                    log.warn("Neúspěšný pokus o přihlášení na terminálu. Nesprávný PIN.");
                    return new InvalidPinException("Zadaný PIN je nesprávný.");
                });

        log.info("Uživatel s ID {} se úspěšně autorizoval na terminálu.", user.getId());
        return user.getId();
    }
}