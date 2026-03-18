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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Oboustranná vazba na MainCategory, která umožňuje získat všechny stanice pro danou kategorii. Na straně MainCategory je definována jako @OneToMany(mappedBy = "category").
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private MainCategory category;

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Maximální počet zaměstnanců, kteří mohou být přiřazeni ke směně na této stanici. Tento atribut je důležitý pro plánování a zajištění dostatečného personálu pro každou směnu. Pokud není nastaven, předpokládá se, že není žádný limit.
     */
    @Column(name = "capacity_limit")
    private Integer capacityLimit;

    /**
     * Oboustranná vazba na Qualification, která umožňuje získat všechny stanice, které vyžadují danou kvalifikaci. Na straně Qualification je definována jako @OneToMany(mappedBy = "reqQualification").
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "req_qualification_id")
    private Qualification reqQualification;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Station)) return false;
        Station that = (Station) o;
        return id!= null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
