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

        String jwt = null;

        // 1. POKUS: Najít JWT token v bezpečné Cookie (pro Web)
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    jwt = cookie.getValue();
                    break;
                }
            }
        }

        // 2. POKUS: Fallback na klasickou hlavičku (pro Terminál nebo starší přístupy)
        if (jwt == null) {
            final String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwt = authHeader.substring(7);
            }
        }

        // Pokud token vůbec nemáme, jdeme dál bez přihlášení (vyhodí to Error 403 na chráněných koncových bodech)
        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Vloženo do try-catch bloku, aby vypršený token neshodil server chybou 500, ale vrátil elegantní 401/403
        try {
            final String userEmail = jwtService.extractUsername(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Tady vracíme náš CustomUserDetails (který obsahuje UUID, e-mail i role)
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    // KRITICKÁ ČÁST PRO IDOR ŠTÍT:
                    // Jako první parametr (Principal) předáváme celý objekt userDetails.
                    // Tím pádem tvá anotace @CheckOwnership může rovnou číst UUID přihlášeného uživatele z paměti.
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Uložíme do Security kontextu
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Pokud je token neplatný (např. vypršel nebo je upravený), potichu to zaznamenáme.
            // Zbytek filtru se postará o to, že uživatel zůstane nepřihlášen.
            log.warn("Nepodařilo se ověřit JWT token (může být vypršený): {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}