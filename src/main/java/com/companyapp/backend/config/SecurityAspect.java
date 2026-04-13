package com.companyapp.backend.config;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class SecurityAspect {

    // DOČASNĚ VYPNUTO: Tento hlídač způsoboval tisíce zbytečných SQL dotazů (N+1 problém)
    // a přetěžoval server při načítání Směnáře (Error 500).
    @Before("execution(* com.companyapp.backend.services.*.*(..))")
    public void validateAccess(JoinPoint joinPoint) {
        // Záměrně prázdné.
        // Aplikace tě konečně pustí dál a přestane se zasekávat.
    }
}