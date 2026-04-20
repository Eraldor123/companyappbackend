package com.companyapp.backend.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final int RESET_MAX_ATTEMPTS = 3; // Max 3 resety za okno

    private final Cache<String, Integer> attemptsCache;
    private final Cache<String, Integer> resetCache; // Nová cache pro resety

    public LoginAttemptService() {
        this.attemptsCache = Caffeine.newBuilder()
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .build();

        // Resety budeme sledovat v 15minutovém okně
        this.resetCache = Caffeine.newBuilder()
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .build();
    }

    // --- LOGIKA PRO PŘIHLÁŠENÍ ---
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
        return attempts != null && attempts >= MAX_ATTEMPTS;
    }

    // --- NOVÁ LOGIKA PRO RESET HESLA ---
    public void registerResetAttempt(String key) {
        int attempts = resetCache.get(key, k -> 0);
        attempts++;
        resetCache.put(key, attempts);
    }

    public boolean isResetRateLimited(String key) {
        Integer attempts = resetCache.getIfPresent(key);
        return attempts != null && attempts >= RESET_MAX_ATTEMPTS;
    }

    public String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}