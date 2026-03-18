package com.companyapp.backend.entity;

import com.companyapp.backend.HashUtil;
import com.companyapp.backend.enums.AccessLevel;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * E-mailová adresa uživatele, která slouží jako unikátní identifikátor pro přihlášení. Tento atribut je klíčový pro autentizaci a komunikaci s uživatelem. E-mail musí být v platném formátu a nesmí být prázdný. Navíc je nastaven jako unikátní a nenulový v databázi, aby se zabránilo duplicitám a zajistila integrita dat.
     */
    @Email(message = "E-mail musí být v platném formátu.")
    @NotBlank(message = "E-mail nesmí být prázdný.")
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    /**
     * PIN kód pro terminál, který slouží jako sekundární metoda autentizace pro přístup k terminálu. Tento kód je důležitý pro zajištění bezpečnosti a kontroly přístupu k terminálu, zejména v situacích, kdy je potřeba rychlý přístup bez nutnosti zadávání e-mailu. PIN musí být unikátní a nesmí být prázdný, aby se zabránilo konfliktům a zajistila integrita dat.
     */
    @NotBlank(message = "PIN kód pro terminál je vyžadován.")
    @Column(name = "pin", unique = true, nullable = false)
    private String pin;

    /**
     * Úroveň přístupu uživatele, která určuje jeho oprávnění a role v systému. Tento atribut je klíčový pro řízení přístupu k různým funkcím a částem aplikace. Úroveň přístupu je reprezentována výčtem (enum) AccessLevel, který může obsahovat hodnoty jako BASIC, MANAGER, ADMIN atd. Tento atribut je uložen v databázi jako řetězec (STRING) a je definován jako nenulový, aby se zajistilo, že každý uživatel má přiřazenou úroveň přístupu.
     */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "role", nullable = false)
    private AccessLevel role;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Oboustranná vazba na UserProfile, která umožňuje získat profil uživatele. Na straně UserProfile je definována jako @OneToOne, kde User je vlastníkem vztahu. Tento vztah je volitelný (optional = true), což znamená, že uživatel nemusí mít přiřazený profil. Kaskádování (cascade = CascadeType.ALL) zajišťuje, že operace provedené na uživateli (např. smazání) se automaticky projeví i na jeho profilu. FetchType.LAZY znamená, že profil bude načten z databáze pouze v případě potřeby, což optimalizuje výkon a snižuje zátěž na systém.
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    private UserProfile userProfile;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return id != null && id.equals(user.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public void setAttendanceId(@NotBlank(message = "Docházkové ID je povinné.") String attendanceId) {
        this.pin = attendanceId;
    }

    public void setAccessLevel(@NotNull(message = "Úroveň přístupu musí být zvolena.") AccessLevel accessLevel) {
        this.role = accessLevel;
    }

    public void setActive(boolean b) {
        this.isActive = b;
    }

    public String getAttendanceId() {
        return this.pin;
    }

     public AccessLevel getAccessLevel() {
        return this.role;
    }

     public boolean isActive() {
        return this.isActive;
    }

    public @Nullable String getPinHash() {
        return HashUtil.hash(this.pin);
    }

    public String getLastName() {
        return userProfile != null ? userProfile.getLastName() : null;
    }

    public String getFirstName() {
        return userProfile != null ? userProfile.getFirstName() : null;
    }
}