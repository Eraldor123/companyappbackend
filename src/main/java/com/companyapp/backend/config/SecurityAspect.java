package com.companyapp.backend.config;

import com.companyapp.backend.entity.User;
import com.companyapp.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
public class SecurityAspect {

    private final UserRepository userRepository;

    // Tato metoda se spustí PŘED jakoukoliv metodou v balíčku services,
    // která bere jako první parametr UUID
    @Before("execution(* com.companyapp.backend.services.*.*(java.util.UUID, ..)) && args(userId, ..)")
    public void checkOwnership(UUID userId) {
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        // Vytáhneme přihlášeného uživatele z DB
        User currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new AccessDeniedException("Uživatel nenalezen"));

        // ... uvnitř metody checkOwnership ...

        // ... uvnitř metody checkOwnership ...

        boolean isManager = currentUser.getRoles().stream()
                .anyMatch(role -> {
                    // Pokud je AccessLevel enum, použijeme metodu .name()
                    String roleName = role.name();
                    return List.of("ADMIN", "PLANNER", "MANAGEMENT").contains(roleName);
                });
        // Pokud to není manažer a ID v požadavku není jeho ID, vyhodíme ho
        if (!isManager && !currentUser.getId().equals(userId)) {
            throw new AccessDeniedException("IDOR detekován: Nemáte oprávnění přistupovat k cizím datům!");
        }
    }
}
