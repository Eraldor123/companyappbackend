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

import java.util.List;
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
        // Načteme aktivní lidi (pro desítky zaměstnanců je to bleskové)
        List<User> activeUsers = userRepository.findAll().stream()
                .filter(User::getIsActive)
                .toList();

        for (User user : activeUsers) {
            // passwordEncoder.matches je jediná správná cesta pro BCrypt
            if (passwordEncoder.matches(rawPin, user.getPin())) {
                log.info("Uživatel s ID {} se úspěšně autorizoval na terminálu.", user.getId());
                return user.getId();
            }
        }

        log.warn("Neúspěšný pokus o přihlášení na terminálu. Nesprávný PIN.");
        throw new InvalidPinException("Zadaný PIN je nesprávný.");
    }
}