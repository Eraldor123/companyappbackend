package com.companyapp.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "main_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor // Přidáno pro podporu Builderu
@Builder // Přidáno pro bezchybnou funkci v PositionSettingsServiceImpl
public class MainCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "main_category_seq")
    @SequenceGenerator(
            name = "main_category_seq",
            sequenceName = "main_category_id_seq",
            allocationSize = 50
    )
    private Integer id;

    @NotBlank(message = "Název kategorie nesmí být prázdný.")
    @Column(name = "name", nullable = false)
    private String name;

    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$",
            message = "Kód barvy musí být platný HEX formát začínající mřížkou a obsahující 3 nebo 6 znaků.")
    @Column(name = "hex_color")
    private String hexColor;

    @Column(name = "sort_order")
    private Integer sortOrder = 1;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * TATO ČÁST CHYBĚLA: Vazba na stanoviště.
     * cascade = ALL a orphanRemoval = true zajistí, že při smazání kategorie
     * zmizí i její stanoviště.
     */
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default // Zajišťuje, že builder nevytvoří null, ale prázdný ArrayList
    private List<Station> stations = new ArrayList<>();

    // --- POMOCNÉ METODY PRO KONZISTENCI VAZBY ---

    public void addStation(Station station) {
        stations.add(station);
        station.setCategory(this);
    }

    public void removeStation(Station station) {
        stations.remove(station);
        station.setCategory(null);
    }

    // --- STANDARDNÍ METODY ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MainCategory)) return false;
        MainCategory that = (MainCategory) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        // Používáme třídu pro stabilitu v Hibernate
        return getClass().hashCode();
    }

    public void setActive(boolean b) {
        this.isActive = b;
    }
}