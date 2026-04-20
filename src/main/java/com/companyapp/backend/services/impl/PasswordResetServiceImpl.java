package com.companyapp.backend.services.impl;

import com.companyapp.backend.entity.PasswordResetToken;
import com.companyapp.backend.entity.User;
import com.companyapp.backend.repository.PasswordResetTokenRepository;
import com.companyapp.backend.repository.UserRepository;
import com.companyapp.backend.services.AuditLogService;
import com.companyapp.backend.services.EmailService;
import com.companyapp.backend.services.dto.request.NewPasswordDto;
import com.companyapp.backend.security.LoginAttemptService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AuditLogService auditLogService;
    private final LoginAttemptService loginAttemptService;
    private final HttpServletRequest request;

    @Transactional
    public void generateResetToken(String email) {
        String ip = loginAttemptService.getClientIP(request);

        // 1. RATE LIMITING: Kontrola IP adresy i E-mailu
        if (loginAttemptService.isResetRateLimited(ip) || loginAttemptService.isResetRateLimited(email)) {
            log.warn("Rate limit aktivován pro reset hesla. IP: {}, Email: {}", ip, email);

            auditLogService.logAction(
                    "PASSWORD_RESET_THROTTLED",
                    "Auth",
                    "N/A",
                    "Požadavek na reset hesla byl zablokován kvůli rate-limitingu (IP/Email: " + email + ")"
            );
            return;
        }

        loginAttemptService.registerResetAttempt(ip);
        loginAttemptService.registerResetAttempt(email);

        Optional<User> userOpt = userRepository.findByEmailAndIsActiveTrue(email);

        if (userOpt.isEmpty()) {
            log.warn("Žádost o reset hesla pro neregistrovaný e-mail: {}", email);
            auditLogService.logAction(
                    "PASSWORD_RESET_ATTEMPT_UNKNOWN",
                    "Auth",
                    "N/A",
                    "Pokus o reset hesla pro neregistrovaný e-mail: " + email
            );
            return;
        }

        User user = userOpt.get();
        PasswordResetToken token = user.getPasswordResetToken();

        if (token == null) {
            token = new PasswordResetToken();
            token.setUser(user);
        }

        token.setToken(UUID.randomUUID().toString());
        token.setExpiryDate(LocalDateTime.now().plusMinutes(15));
        tokenRepository.save(token);

        auditLogService.logAction(
                "PASSWORD_RESET_REQUESTED",
                "User",
                user.getId().toString(),
                "Uživatel " + user.getEmail() + " požádal o odkaz pro obnovu hesla."
        );

        String resetLink = "http://localhost:5173/reset-hesla?token=" + token.getToken();
        emailService.sendResetPasswordEmail(user.getEmail(), resetLink);

        log.info("E-mail pro reset hesla byl odeslán uživateli: {}", email);
    }

    @Transactional
    public void resetPassword(NewPasswordDto request) {
        PasswordResetToken resetToken = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Neplatný nebo neexistující odkaz pro obnovu hesla."));

        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            throw new IllegalArgumentException("Platnost tohoto odkazu již vypršela.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        // --- OPRAVA CHYBY: Odpojíme token z paměti uživatele, aby z toho Hibernate nezešílel ---
        user.setPasswordResetToken(null);

        userRepository.save(user);

        auditLogService.logAction(
                "PASSWORD_CHANGED_SUCCESS",
                "User",
                user.getId().toString(),
                "Uživatel " + user.getEmail() + " si úspěšně změnil heslo."
        );

        tokenRepository.delete(resetToken);
        log.info("Heslo pro uživatele {} bylo úspěšně změněno.", user.getEmail());
    }
}