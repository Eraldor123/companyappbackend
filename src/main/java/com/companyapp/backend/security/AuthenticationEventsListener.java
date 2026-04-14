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

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        String ip = loginAttemptService.getClientIP(request);
        loginAttemptService.loginSucceeded(ip);
    }

    @EventListener
    public void onFailure(AuthenticationFailureBadCredentialsEvent event) {
        String ip = loginAttemptService.getClientIP(request);
        loginAttemptService.loginFailed(ip);
    }
}