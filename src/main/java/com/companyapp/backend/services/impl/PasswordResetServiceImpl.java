package com.companyapp.backend.services.impl;

import com.companyapp.backend.entity.PasswordResetToken;
import com.companyapp.backend.entity.User;
import com.companyapp.backend.repository.PasswordResetTokenRepository;
import com.companyapp.backend.repository.UserRepository;
import com.companyapp.backend.services.EmailService;
import com.companyapp.backend.services.dto.request.NewPasswordDto;
import com.companyapp.backend.services.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService; // PŘIDÁNO

    @Transactional
    public void generateResetToken(String email) {
        User user = userRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new ResourceNotFoundException("Uživatel s tímto e-mailem nebyl nalezen."));

        tokenRepository.deleteByUser(user);

        PasswordResetToken token = new PasswordResetToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setExpiryDate(LocalDateTime.now().plusMinutes(15));
        tokenRepository.save(token);

        String resetLink = "http://localhost:5173/reset-hesla?token=" + token.getToken();

        // REÁLNÉ ODESLÁNÍ
        emailService.sendResetPasswordEmail(user.getEmail(), resetLink);

        log.info("E-mail pro reset hesla byl odeslán uživateli: {}", email);
    }

    @Transactional
    public void resetPassword(NewPasswordDto request) {
        // 1. Ověříme, že token existuje a nevypršel
        PasswordResetToken resetToken = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Neplatný nebo neexistující odkaz pro obnovu hesla."));

        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            throw new IllegalArgumentException("Platnost tohoto odkazu již vypršela. Vyžádejte si nový.");
        }

        // 2. Nastavíme nové HESLO (už to není PIN!)
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword())); // ZMĚNĚNO z setPin na setPassword
        userRepository.save(user);

        // 3. Smažeme použitý token
        tokenRepository.delete(resetToken);
        log.info("Heslo pro uživatele {} bylo úspěšně změněno.", user.getEmail());
    }
}