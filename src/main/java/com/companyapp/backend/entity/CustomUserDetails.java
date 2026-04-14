package com.companyapp.backend.entity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class CustomUserDetails implements UserDetails {

    // Ukládáme si pouze "čisté" textové hodnoty, NE celou databázovou entitu
    private final UUID id;
    private final String username;
    private final String password;
    private final boolean isActive;
    private final List<GrantedAuthority> authorities;

    public CustomUserDetails(User user) {
        // Hned při vytvoření si vyzobeme potřebná data a entitu "user" zahodíme
        this.id = user.getId();
        this.username = user.getEmail();
        this.password = user.getPasswordHash(); // Použito tvé getPasswordHash()
        this.isActive = user.getIsActive();

        // Převedeme role a uložíme je do paměti (odstřiženo od Hibernate)
        this.authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toList());
    }

    // PŘIDÁNO PRO BEZPEČNOSTNÍ KONTROLY (IDOR)
    public UUID getId() {
        return id; // Vracíme přímo zkopírované UUID
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
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return isActive; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return isActive; }
}