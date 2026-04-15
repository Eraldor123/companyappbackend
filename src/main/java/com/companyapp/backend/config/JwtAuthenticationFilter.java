package com.companyapp.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Získání tokenu (extrahováno do samostatné metody pro snížení kognitivní složitosti)
        String jwt = extractJwtFromRequest(request);

        // 2. Pokud token máme, pokusíme se uživatele přihlásit
        if (jwt != null) {
            try {
                authenticateUserFromToken(jwt, request);
            } catch (Exception e) {
                // Pokud je token neplatný (např. vypršel nebo je upravený), potichu to zaznamenáme.
                log.warn("Nepodařilo se ověřit JWT token (může být vypršený): {}", e.getMessage());
            }
        }

        // 3. Pokračujeme v řetězci filtrů
        filterChain.doFilter(request, response);
    }

    /**
     * Pomocná metoda pro nalezení JWT tokenu v Cookies nebo v hlavičce.
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        // POKUS 1: Najít JWT token v bezpečné Cookie (pro Web)
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        // POKUS 2: Fallback na klasickou hlavičku (pro Terminál nebo starší přístupy)
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null; // Token nenalezen
    }

    /**
     * Pomocná metoda pro ověření tokenu a naplnění SecurityContextu.
     */
    private void authenticateUserFromToken(String jwt, HttpServletRequest request) {
        String userEmail = jwtService.extractUsername(jwt);

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
    }
}