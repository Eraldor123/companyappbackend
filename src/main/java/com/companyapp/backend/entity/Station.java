package com.companyapp.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor // Přidáno pro podporu Builderu
@Builder // Přidáno pro bezchybnou funkci v servisu a DTO mapování
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

    /**
     * TATO ČÁST CHYBĚLA: Vazba na šablony směn.
     * Zajišťuje, že když načteš stanoviště, stáhnou se k němu i jeho šablony.
     * cascade = ALL a orphanRemoval = true zajistí automatické smazání šablon při smazání stanoviště.
     */
    @OneToMany(mappedBy = "station", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default // Zajišťuje, že builder nevytvoří null, ale prázdný ArrayList
    private List<ShiftTemplate> templates = new ArrayList<>();

    // --- POMOCNÉ METODY PRO KONZISTENCI VAZBY ---

    public void addTemplate(ShiftTemplate template) {
        templates.add(template);
        template.setStation(this);
    }

    public void removeTemplate(ShiftTemplate template) {
        templates.remove(template);
        template.setStation(null);
    }

    // --- STANDARDNÍ METODY ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Station)) return false;
        Station that = (Station) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        // Používáme getClass().hashCode() pro prevenci problémů s Hibernate proxies
        return getClass().hashCode();
    }

    public void setActive(boolean b) {
        this.isActive = b;
    }
}