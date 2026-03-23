package com.companyapp.backend.entity;

import com.companyapp.backend.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    /**
     * Mapování nativní enumerace AccessLevel (úroveň oprávnění)
     * na autoritu podporovanou frameworkem Spring Security.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Záměrně použijeme pin kód z entity User pro případnou autentizaci hardwarových terminálů
     * či dalších rozhraní podle specifikačně zadaných parametrů.
     */
    @Override
    public String getPassword() {
        return user.getPin();
    }

    /**
     * Pro uživatelské jméno použijeme email, protože je unikátní a snadno zapamatovatelný.
     * Toto rozhodnutí je v souladu s běžnými praktikami pro autentizaci uživatelů.
     */
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    /**
     * Všechny následující metody vrací true, protože v našem modelu není implementována logika pro expirované účty,
     * @return true pro všechny stavy účtu, protože nejsou implementovány žádné restrikce
     */
    @Override
    public boolean isAccountNonExpired() { return true; }

    /**
     * Zde kontrolujeme, zda je účet aktivní. Pokud není aktivní, považujeme ho za zablokovaný.
     * Toto je jednoduchý způsob, jak implementovat základní správu stavu účtu bez potřeby složitější logiky.
     * @return true pokud je účet aktivní, jinak false
     */
    @Override
    public boolean isAccountNonLocked() { return user.getIsActive(); }

    /**
     * V našem modelu není implementována logika pro expirované přihlašovací údaje, takže vždy vracíme true.
     * Toto je záměrné rozhodnutí, protože v současné fázi vývoje není potřeba řešit expirované přihlašovací údaje.
     * @return true, protože nejsou implementovány žádné restrikce pro expirované přihlašovací údaje
     */
    @Override
    public boolean isCredentialsNonExpired() { return true; }

    /**
     * Zde opět kontrolujeme, zda je účet aktivní. Pokud není aktivní, považujeme ho za neaktivní.
     * @return true pokud je účet aktivní, jinak false
     */
    @Override
    public boolean isEnabled() { return user.getIsActive(); }
}
