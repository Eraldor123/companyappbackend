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

    // OPRAVA java:S1192: Konstanty pro opakované řetězce
    private static final String AUTH_KEY = "authenticated";
    private static final String JWT_COOKIE_NAME = "jwt";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    /**
     * Ověřuje platnost JWT tokenu z cookie.
     * OPRAVENO: Odstraněny duplicitní literály a zploštěna logika.
     */
    @GetMapping("/verify")
    public ResponseEntity<Map<String, Boolean>> verifyToken(
            @CookieValue(name = JWT_COOKIE_NAME, required = false) String token) {

        // Pokud cookie chybí nebo je prázdná, vracíme false
        if (token == null || token.isEmpty()) {
            return ResponseEntity.ok(Map.of(AUTH_KEY, false));
        }

        try {
            String userEmail = jwtService.extractUsername(token);

            if (userEmail != null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                if (jwtService.isTokenValid(token, userDetails)) {
                    return ResponseEntity.ok(Map.of(AUTH_KEY, true));
                }
            }
        } catch (Exception e) {
            // Ignorujeme chybu (např. neplatný formát tokenu) a vracíme false
            // NOSONAR: Prázdný catch je zde záměrný
        }

        return ResponseEntity.ok(Map.of(AUTH_KEY, false));
    }
}