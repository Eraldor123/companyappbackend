package com.companyapp.backend.security;

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

    /**
     * OPRAVA java:S1172: Parametr 'event' je nezbytný pro identifikaci události Springem.
     * V budoucnu lze z event.getAuthentication().getName() získat login uživatele pro audit.
     */
    @EventListener
    public void onSuccess(@SuppressWarnings("unused") AuthenticationSuccessEvent event) {
        String ip = loginAttemptService.getClientIP(request);
        loginAttemptService.loginSucceeded(ip);
    }

    /**
     * OPRAVA java:S1172: Parametr 'event' potlačen, ale zachován pro budoucí logging
     * neúspěšných pokusů konkrétních uživatelských jmen.
     */
    @EventListener
    public void onFailure(@SuppressWarnings("unused") AuthenticationFailureBadCredentialsEvent event) {
        String ip = loginAttemptService.getClientIP(request);
        loginAttemptService.loginFailed(ip);
    }
}