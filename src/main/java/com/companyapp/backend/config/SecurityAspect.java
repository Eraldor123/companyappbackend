package com.companyapp.backend.config;

import com.companyapp.backend.entity.CustomUserDetails;
import com.companyapp.backend.services.dto.request.Ownable;
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

@Aspect
@Component
@Order(1) // Běží ÚPLNĚ PRVNÍ, ještě před @Transactional, aby šetřil DB spojení
public class SecurityAspect {

    // Cache pro bleskové vyhledání indexů parametrů (vyřeší výkonnostní problém)
    private final Map<Method, List<Integer>> parameterIndicesCache = new ConcurrentHashMap<>();

    @Before("execution(* com.companyapp.backend.services..*(..))")
    public void validateDirectObjectReference(JoinPoint joinPoint) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            // 1. Pokud není nikdo přihlášen, nestaráme se (řeší to SecurityConfig)
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                return;
            }

            // 2. Extrahujeme principal objekt z paměti
            if (!(auth.getPrincipal() instanceof CustomUserDetails currentUser)) {
                return;
            }

            // OCHRANA 1: Co když uživatel nemá v tokenu/databázi UUID?
            UUID currentUserId = currentUser.getId();
            if (currentUserId == null) {
                System.err.println("⚠️ Uživatel " + currentUser.getUsername() + " je přihlášen, ale jeho UUID je NULL! Zkontroluj UserDetailsService.");
                return;
            }

            // 3. Bypass pro nadřazené role (Admin, Management a Terminál mohou upravovat vše)
            if (isUserAuthorizedByRole(auth, "ROLE_ADMIN") ||
                    isUserAuthorizedByRole(auth, "ROLE_MANAGEMENT") ||
                    isUserAuthorizedByRole(auth, "ROLE_TERMINAL")) {
                return;
            }

            // OCHRANA 2: Jistota, že zachytáváme standardní metodu (ne konstruktor nebo proxy)
            if (!(joinPoint.getSignature() instanceof MethodSignature signature)) {
                return;
            }

            Method method = signature.getMethod();
            Object[] args = joinPoint.getArgs();

            // 4. Jdeme hledat anotace @CheckOwnership u parametrů volané metody
            List<Integer> targetIndices = parameterIndicesCache.computeIfAbsent(method, this::scanMethodAnnotations);

            // 5. Validace nalezených parametrů
            for (Integer index : targetIndices) {
                // OCHRANA 3: Jistota, že parametr skutečně existuje v poli argumentů
                if (index < args.length) {
                    checkOwnership(args[index], currentUserId);
                }
            }

        } catch (AccessDeniedException e) {
            // Tuto výjimku CHCEME propustit dál – znamená to, že IDOR štít zachytil neoprávněný přístup
            throw e;
        } catch (Exception e) {
            // Všechny ostatní nečekané chyby zachytíme, aby neshodily načítání dat pro uživatele
            System.err.println("❌ KRITICKÁ CHYBA V SECURITY ASPECTU u metody: " + joinPoint.getSignature().getName());
            e.printStackTrace();
        }
    }

    private void checkOwnership(Object value, UUID currentUserId) {
        if (value == null) {
            throw new AccessDeniedException("Bezpečnostní chyba: Chybí identifikátor pro ověření vlastnictví.");
        }

        // Scénář A: Parametr je přímo UUID
        if (value instanceof UUID targetUuid) {
            if (!targetUuid.equals(currentUserId)) {
                throw new AccessDeniedException("IDOR Ochrana: Nemáte oprávnění k datům cizího uživatele!");
            }
        }
        // Scénář B: Parametr je DTO, které implementuje rozhraní Ownable
        else if (value instanceof Ownable ownableDto) {
            if (!ownableDto.getOwnerId().equals(currentUserId)) {
                throw new AccessDeniedException("IDOR Ochrana: Tento objekt obsahuje cizí identifikátor!");
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
            for (Annotation a : paramAnnotations[i]) {
                if (a instanceof CheckOwnership) {
                    indices.add(i);
                }
            }
        }
        return indices;
    }
}