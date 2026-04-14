package com.companyapp.backend.entity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Wrapper nad entitou User pro Spring Security.
 * Ukládá si pouze nezbytná data v paměti, aby se předešlo problémům s Hibernate session.
 */
public class CustomUserDetails implements UserDetails {

    private final UUID id;
    private final String username;
    private final String password;
    private final boolean isActive;
    private final List<GrantedAuthority> authorities;

    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.username = user.getEmail();
        // ZMĚNA: Použij standardní getter, který ti vrací heslo z DB (již v BCryptu)
        this.password = user.getPassword();
        this.isActive = user.getIsActive();
        // ... zbytek kódu ...


        // Převedeme AccessLevel enumy na GrantedAuthority pro Spring Security
        this.authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toList());
    }

    /**
     * Klíčová metoda pro náš bezpečnostní aspekt @CheckOwnership (IDOR ochrana).
     * Umožňuje nám porovnat ID v requestu s ID přihlášeného uživatele.
     */
    public UUID getId() {
        return id;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isActive; // Pokud není uživatel aktivní, účet je zamčený
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }
}