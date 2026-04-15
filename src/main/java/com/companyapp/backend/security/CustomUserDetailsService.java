package com.companyapp.backend.security;

import com.companyapp.backend.entity.CustomUserDetails;
import com.companyapp.backend.entity.User;
import com.companyapp.backend.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final LoginAttemptService loginAttemptService;
    private final HttpServletRequest request;

    // OPRAVA: Odstraněny nepoužité importy Autowired a RequiredArgsConstructor.
    // Ponecháváme ruční konstruktor s @Lazy pro vyřešení kruhové závislosti.
    public CustomUserDetailsService(
            UserRepository userRepository,
            LoginAttemptService loginAttemptService,
            @Lazy HttpServletRequest request) {
        this.userRepository = userRepository;
        this.loginAttemptService = loginAttemptService;
        this.request = request;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Získáme IP adresu z requestu
        String ip = loginAttemptService.getClientIP(request);

        // 2. Kontrola Brute-force ochrany
        // OPRAVA java:S112: Místo obecné RuntimeException házíme specifickou LockedException.
        // Spring Security ji automaticky zachytí a převede na správnou chybovou hlášku.
        if (loginAttemptService.isBlocked(ip)) {
            throw new LockedException("Vaše IP adresa byla dočasně zablokována kvůli mnoha neúspěšným pokusům.");
        }

        // 3. Načtení uživatele
        User user = userRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new UsernameNotFoundException("Uživatel s emailem " + email + " nebyl nalezen nebo je neaktivní."));

        // 4. Vrácení implementace UserDetails
        return new CustomUserDetails(user);
    }
}