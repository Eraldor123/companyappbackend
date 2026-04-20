package com.companyapp.backend.security;

import com.companyapp.backend.services.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationEventsListener {

    private final LoginAttemptService loginAttemptService;
    private final HttpServletRequest request;
    private final AuditLogService auditLogService; // PŘIDÁNO: Služba pro zápis do Audit Logu

    /**
     * OPRAVA java:S1172: Parametr 'event' je nezbytný pro identifikaci události Springem.
     * Využito: Z event.getAuthentication().getName() získáváme login uživatele pro audit.
     */
    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        String ip = loginAttemptService.getClientIP(request);
        loginAttemptService.loginSucceeded(ip);

        // PŘIDÁNO: Zapsání úspěšného přihlášení do Audit Logu
        String username = event.getAuthentication().getName();
        auditLogService.logAction(
                "LOGIN_SUCCESS",
                "Auth",
                username,
                "Úspěšné přihlášení z IP adresy: " + ip
        );
    }

    /**
     * OPRAVA java:S1172: Parametr 'event' je využit pro logging
     * neúspěšných pokusů konkrétních uživatelských jmen.
     */
    @EventListener
    public void onFailure(AuthenticationFailureBadCredentialsEvent event) {
        String ip = loginAttemptService.getClientIP(request);
        loginAttemptService.loginFailed(ip);

        // PŘIDÁNO: Zapsání chybného pokusu do Audit Logu
        String username = event.getAuthentication().getName();
        auditLogService.logAction(
                "LOGIN_FAILED",
                "Auth",
                username,
                "Neúspěšný pokus o přihlášení (špatné heslo) z IP: " + ip
        );
    }
}