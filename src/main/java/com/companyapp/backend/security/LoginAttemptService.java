package com.companyapp.backend.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {

    /**
     * OPRAVA java:S1170 & java:S115:
     * Přidáno 'static', aby byla konstanta sdílená napříč instancemi (šetří paměť).
     * Přejmenováno na MAX_ATTEMPTS (množné číslo a UPPER_SNAKE_CASE odpovídá standardům pro konstanty).
     */
    private static final int MAX_ATTEMPTS = 5;

    private final Cache<String, Integer> attemptsCache;

    public LoginAttemptService() {
        // OPRAVA: Odstraněno redundantní super()
        // Cache se automaticky vymaže po 15 minutách od posledního zápisu
        this.attemptsCache = Caffeine.newBuilder()
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .build();
    }

    public void loginSucceeded(String key) {
        attemptsCache.invalidate(key);
    }

    public void loginFailed(String key) {
        int attempts = attemptsCache.get(key, k -> 0);
        attempts++;
        attemptsCache.put(key, attempts);
    }

    public boolean isBlocked(String key) {
        Integer attempts = attemptsCache.getIfPresent(key);
        // OPRAVA: Použití nové konstanty MAX_ATTEMPTS
        return attempts != null && attempts >= MAX_ATTEMPTS;
    }

    /**
     * Pomocná metoda pro získání IP adresy i přes proxy (Cloudflare/Nginx).
     */
    public String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}