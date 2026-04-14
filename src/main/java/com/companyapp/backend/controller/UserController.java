package com.companyapp.backend.controller;

import com.companyapp.backend.config.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @GetMapping("/verify")
    public ResponseEntity<Map<String, Boolean>> verifyToken(@CookieValue(name = "jwt", required = false) String token) {
        // Pokud cookie chybí, vrátíme 200 OK s informací "false"
        if (token == null || token.isEmpty()) {
            return ResponseEntity.ok(Map.of("authenticated", false));
        }

        try {
            String userEmail = jwtService.extractUsername(token);
            if (userEmail != null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                if (jwtService.isTokenValid(token, userDetails)) {
                    // Platný token -> vracíme true
                    return ResponseEntity.ok(Map.of("authenticated", true));
                }
            }
        } catch (Exception e) {
            // Ignorujeme chybu a vracíme false
        }

        return ResponseEntity.ok(Map.of("authenticated", false));
    }
}