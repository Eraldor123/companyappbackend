package com.companyapp.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
@Getter
@Setter
public class UserQualificationId implements Serializable {

    /**
     * UUID uživatele, který má danou kvalifikaci. Tento atribut je součástí složeného primárního klíče a zároveň slouží jako cizí klíč na entitu User. Na straně User je definována jako @OneToMany(mappedBy = "user").
     */
    @Column(name = "user_id")
    private UUID userId;

    /**
     * ID kvalifikace, kterou uživatel má. Tento atribut je součástí složeného primárního klíče a zároveň slouží jako cizí klíč na entitu Qualification. Na straně Qualification je definována jako @OneToMany(mappedBy = "qualification").
     */
    @Column(name = "qualification_id")
    private Integer qualificationId;

    /**
     * Výchozí konstruktor je potřeba pro JPA, aby mohl vytvořit instanci této třídy při načítání dat z databáze. Bez tohoto konstruktoru by JPA nemohlo správně fungovat a načítat data do této třídy.
     */
    public UserQualificationId() {}

    /**
     * Konstruktor pro snadné vytváření instancí této třídy s danými hodnotami. Tento konstruktor umožňuje rychle vytvořit instanci UserQualificationId s konkrétním userId a qualificationId, což je užitečné při práci s entitou UserQualification, která používá tuto třídu jako svůj primární klíč.
     * @param userId UUID uživatele, který má danou kvalifikaci. Tento atribut je součástí složeného primárního klíče a zároveň slouží jako cizí klíč na entitu User. Na straně User je definována jako @OneToMany(mappedBy = "user").
     * @param qualificationId ID kvalifikace, kterou uživatel má. Tento atribut je součástí složeného primárního klíče a zároveň slouží jako cizí klíč na entitu Qualification. Na straně Qualification je definována jako @OneToMany(mappedBy = "qualification").
     */
    public UserQualificationId(UUID userId, Integer qualificationId) {
        this.userId = userId;
        this.qualificationId = qualificationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserQualificationId)) return false;
        UserQualificationId that = (UserQualificationId) o;
        return Objects.equals(getUserId(), that.getUserId()) &&
                Objects.equals(getQualificationId(), that.getQualificationId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserId(), getQualificationId());
    }
}
