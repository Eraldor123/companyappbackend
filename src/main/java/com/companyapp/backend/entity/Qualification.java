package com.companyapp.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "qualifications")
@Getter
@Setter
@NoArgsConstructor
public class Qualification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Oboustranná vazba na UserQualification, která umožňuje získat všechny uživatele, kteří mají tuto kvalifikaci.
     */
    @OneToMany(mappedBy = "qualification", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserQualification> userQualifications = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Qualification)) return false;
        Qualification that = (Qualification) o;
        return id!= null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
