package com.companyapp.backend.controller;

import com.companyapp.backend.services.dto.request.NewPasswordDto;
import com.companyapp.backend.services.dto.request.PasswordResetRequestDto;
import com.companyapp.backend.services.impl.PasswordResetServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/password-reset")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetServiceImpl passwordResetService;

    // Krok 1: Uživatel zadá email a my vygenerujeme odkaz
    @PostMapping("/request")
    public ResponseEntity<Void> requestPasswordReset(@Valid @RequestBody PasswordResetRequestDto request) {
        passwordResetService.generateResetToken(request.getEmail());
        // Vždy vracíme 200 OK z bezpečnostních důvodů (aby hacker nezjistil, jestli email existuje nebo ne)
        return ResponseEntity.ok().build();
    }

    // Krok 2: Uživatel posílá nové heslo spolu s tajným tokenem
    @PostMapping("/confirm")
    public ResponseEntity<Void> confirmPasswordReset(@Valid @RequestBody NewPasswordDto request) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.ok().build();
    }
}