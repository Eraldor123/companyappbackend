package com.companyapp.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
public class UserProfile {

    /**
     * OPRAVA: Změněno jméno sloupce na 'user_id'.
     * Protože používáš @MapsId, primární klíč profilu je zároveň cizím klíčem
     * odkazujícím na uživatele. V DB se tento sloupec jmenuje 'user_id'.
     */
    @Id
    @Column(name = "user_id")
    private UUID id;

    /**
     * Vazba na User se sdíleným primárním klíčem.
     *
     */
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @NotBlank(message = "Jméno je povinné.")
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank(message = "Příjmení je povinné.")
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    /**
     * OPRAVA java:S6201: Použití Pattern Matching pro instanceof.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserProfile that)) return false;

        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}