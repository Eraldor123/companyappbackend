package com.companyapp.backend.config;

import com.companyapp.backend.entity.CustomUserDetails;
import com.companyapp.backend.services.dto.request.Ownable;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Aspect
@Component
@Order(1)
public class SecurityAspect {

    private final Map<Method, List<Integer>> parameterIndicesCache = new ConcurrentHashMap<>();

    @Before("execution(* com.companyapp.backend.services..*(..))")
    public void validateDirectObjectReference(JoinPoint joinPoint) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            // 1. Základní guardy pro neautorizovaný přístup
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                return;
            }

            // 2. Kontrola identity a UUID
            if (!(auth.getPrincipal() instanceof CustomUserDetails currentUser) || currentUser.getId() == null) {
                return;
            }

            // 3. Bypass pro privilegované role (Admin, Management, Terminál)
            if (isUserAuthorizedByRole(auth, "ROLE_ADMIN") ||
                    isUserAuthorizedByRole(auth, "ROLE_MANAGEMENT") ||
                    isUserAuthorizedByRole(auth, "ROLE_TERMINAL")) {
                return;
            }

            // 4. Extrakce a validace parametrů metody
            if (joinPoint.getSignature() instanceof MethodSignature signature) {
                validateParameters(signature.getMethod(), joinPoint.getArgs(), currentUser.getId());
            }

        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ KRITICKÁ CHYBA V SECURITY ASPECTU u metody: {}", joinPoint.getSignature().getName(), e);
        }
    }

    private void validateParameters(Method method, Object[] args, UUID currentUserId) {
        List<Integer> targetIndices = parameterIndicesCache.computeIfAbsent(method, this::scanMethodAnnotations);

        for (Integer index : targetIndices) {
            if (index < args.length) {
                checkOwnership(args[index], currentUserId);
            }
        }
    }

    /**
     * OPRAVENO: Implementace Java 21 Guarded Patterns.
     * Využívá 'when' pro eliminaci vnitřních if podmínek v switchi.
     */
    private void checkOwnership(Object value, UUID currentUserId) {
        switch (value) {
            case null ->
                    throw new AccessDeniedException("Bezpečnostní chyba: Chybí identifikátor pro ověření vlastnictví.");

            // Použití Guarded Pattern (when) - moderní standard Javy 21
            case UUID targetUuid when !targetUuid.equals(currentUserId) ->
                    throw new AccessDeniedException("IDOR Ochrana: Nemáte oprávnění k datům cizího uživatele!");

            case Ownable ownableDto when !ownableDto.getOwnerId().equals(currentUserId) ->
                    throw new AccessDeniedException("IDOR Ochrana: Tento objekt obsahuje cizí identifikátor!");

            default -> {
                // Objekty, které projdou validací nebo nejsou Ownable/UUID, jsou ignorovány
            }
        }
    }

    private boolean isUserAuthorizedByRole(Authentication auth, String requiredRole) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals(requiredRole));
    }

    private List<Integer> scanMethodAnnotations(Method method) {
        List<Integer> indices = new ArrayList<>();
        Annotation[][] paramAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < paramAnnotations.length; i++) {
            indices.addAll(scanParameter(paramAnnotations[i], i));
        }
        return indices;
    }

    private List<Integer> scanParameter(Annotation[] annotations, int index) {
        List<Integer> foundIndices = new ArrayList<>();
        for (Annotation a : annotations) {
            if (a instanceof CheckOwnership) {
                foundIndices.add(index);
            }
        }
        return foundIndices;
    }
}