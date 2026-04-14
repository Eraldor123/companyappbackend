package com.companyapp.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "stations")
@Getter
@Setter
@NoArgsConstructor
public class Station {

    @Id
    /**
     * FÁZE 2: Optimalizace transakcí u generátorů ID.
     * Změna z IDENTITY na SEQUENCE umožňuje Hibernate 6 používat dávkové vkládání.
     * allocationSize = 50 zajišťuje, že se ID poptávají z DB v dávkách, nikoliv po jednom.
     */
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "station_seq")
    @SequenceGenerator(
            name = "station_seq",
            sequenceName = "station_id_seq",
            allocationSize = 50
    )
    private Integer id;

    /**
     * Oboustranná vazba na MainCategory, která umožňuje získat všechny stanice pro danou kategorii.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private MainCategory category;

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Maximální počet zaměstnanců, kteří mohou být přiřazeni ke směně na této stanici.
     */
    @Column(name = "capacity_limit")
    private Integer capacityLimit;

    /**
     * Indikátor, zda je pro práci na této stanici vyžadována specifická kvalifikace (true/false).
     */
    @Column(name = "needs_qualification", nullable = false)
    private Boolean needsQualification = false;

    /**
     * Indikátor, zda je stanoviště aktivní.
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "sort_order")
    private Integer sortOrder = 1;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Station)) return false;
        Station that = (Station) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public void setActive(boolean b) {
        this.isActive = b;
    }
}