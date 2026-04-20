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

        try {
            // 1. Získání tokenu (Hlavička nebo Cookie)
            String jwt = extractJwtFromRequest(request);

            // 2. Pokud token existuje a uživatel ještě není přihlášený v kontextu
            if (jwt != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                String userEmail = jwtService.extractUsername(jwt);

                if (userEmail != null) {
                    UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                    // 3. Validace tokenu
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
        } catch (Exception e) {
            // Zachytíme chyby (např. expirovaný nebo podvržený token).
            // Aplikace nespadne, jen potichu varuje a uživatele nepustí dál.
            log.warn("Nepodařilo se ověřit JWT token: {}", e.getMessage());
        }

        // 4. Pokračujeme v řetězci filtrů
        filterChain.doFilter(request, response);
    }

    /**
     * Pomocná metoda: Nejdřív zkusí hlavičku (pro LocalStorage), pak Cookie (pro Cookies).
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        // POKUS 1: Klasická hlavička Authorization (tvůj původní systém / Postman)
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // POKUS 2: Bezpečná HttpOnly Cookie (nový systém)
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null; // Token nenalezen
    }
}