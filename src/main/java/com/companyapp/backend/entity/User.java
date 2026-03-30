package com.companyapp.backend.entity;

import com.companyapp.backend.HashUtil;
import com.companyapp.backend.enums.AccessLevel;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

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

    @Email(message = "E-mail musí být v platném formátu.")
    @NotBlank(message = "E-mail nesmí být prázdný.")
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @NotBlank(message = "PIN kód pro terminál je vyžadován.")
    @Column(name = "pin", nullable = false)
    private String pin;
    // --- PŘIDÁNO: Nové heslo pro web ---
    @Column(name = "password") // Zatím necháme nullable, pro případ starých dat
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private java.util.Set<AccessLevel> roles = new java.util.HashSet<>();

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // --- NAŠE PŘIDANÁ VAZBA NA STANOVIŠTĚ ---
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_qualified_stations",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "station_id")
    )
    private java.util.Set<Station> qualifiedStations = new java.util.HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, optional = true)
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

    public void setActive(boolean b) {
        this.isActive = b;
    }

    public boolean isActive() {
        return this.isActive;
    }

    public String getPinHash() {
        return HashUtil.hash(this.pin);
    }
    public String getPasswordHash() { return this.password; }

    public String getLastName() {
        return userProfile != null ? userProfile.getLastName() : null;
    }

    public String getFirstName() {
        return userProfile != null ? userProfile.getFirstName() : null;
    }

    @OneToOne(mappedBy = "user", optional = false)
    private PasswordResetToken passwordResetToken;

    public PasswordResetToken getPasswordResetToken() {
        return passwordResetToken;
    }

    public void setPasswordResetToken(PasswordResetToken passwordResetToken) {
        this.passwordResetToken = passwordResetToken;
    }
}