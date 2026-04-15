package com.companyapp.backend.entity;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Wrapper nad entitou User pro Spring Security.
 * OPRAVENO: Typová nekompatibilita u Streamu a přidán Lombok @Getter.
 */
@Getter // Vyřeší "Field 'id' may have Lombok @Getter" i pro username a password
public class CustomUserDetails implements UserDetails {

    private final UUID id;
    private final String username;
    private final String password;
    private final boolean isActive;
    private final List<GrantedAuthority> authorities;

    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.username = user.getEmail();
        this.password = user.getPassword();
        this.isActive = user.getIsActive();

        /*
         * OPRAVA: Přidáno explicitní přetypování na <GrantedAuthority>,
         * aby .toList() vrátil správný typ seznamu.
         */
        this.authorities = user.getRoles().stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role.name()))
                .toList();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    // Metody getUsername(), getPassword() a getId() už nemusíš psát ručně,
    // Lombok @Getter je vygeneruje za tebe na pozadí.

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isEnabled();
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