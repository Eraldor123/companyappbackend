package com.companyapp.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "main_categories")
@Getter
@Setter
@NoArgsConstructor
public class MainCategory {

    @Id
    /**
     * FÁZE 2: Optimalizace transakcí u generátorů ID.
     * Změna z IDENTITY na SEQUENCE pro podporu batchingu v Hibernate 6.
     * allocationSize = 50 zajišťuje, že se ID nepoptávají v DB po jednom, což šetří výkon.
     */
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

    /**
     * HEX kód barvy pro zobrazení kategorie. Musí být ve formátu #RRGGBB nebo #RGB.
     */
    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$",
            message = "Kód barvy musí být platný HEX formát začínající mřížkou a obsahující 3 nebo 6 znaků.")
    @Column(name = "hex_color")
    private String hexColor;

    /**
     * Pořadí pro zobrazení v uživatelském rozhraní.
     */
    @Column(name = "sort_order")
    private Integer sortOrder = 1;

    /**
     * Indikátor, zda je kategorie aktivní.
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MainCategory)) return false;
        MainCategory that = (MainCategory) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    // Ponecháno pro kompatibilitu se stávajícím kódem
    public void setActive(boolean b) {
        this.isActive = b;
    }
}