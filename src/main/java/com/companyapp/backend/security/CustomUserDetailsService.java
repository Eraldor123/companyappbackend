package com.companyapp.backend.security;

import com.companyapp.backend.entity.User;
import com.companyapp.backend.repository.UserRepository;
import com.companyapp.backend.entity.CustomUserDetails; // Ujisti se, že se takto jmenuje tvůj UserDetails wrapper
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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

    // Používáme ruční konstruktor nebo @Autowired s @Lazy, aby nedošlo k zacyklení při startu
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
        // 1. Získáme IP adresu z requestu přes naši novou službu
        String ip = loginAttemptService.getClientIP(request);

        // 2. Kontrola, zda IP adresa není zablokovaná (Brute-force ochrana)
        if (loginAttemptService.isBlocked(ip)) {
            throw new RuntimeException("Vaše IP adresa byla dočasně zablokována kvůli mnoha neúspěšným pokusům.");
        }

        // 3. Načtení uživatele z databáze
        User user = userRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new UsernameNotFoundException("Uživatel s emailem " + email + " nebyl nalezen nebo je neaktivní."));

        // 4. Vrácení implementace UserDetails
        return new CustomUserDetails(user);
    }
}