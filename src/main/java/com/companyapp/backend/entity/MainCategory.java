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
@AllArgsConstructor
@Builder
public class MainCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "main_category_seq")
    @SequenceGenerator(
            name = "main_category_seq",
            sequenceName = "main_category_id_seq"
    )
    private Integer id;

    @NotBlank(message = "Název kategorie nesmí být prázdný.")
    @Column(name = "name", nullable = false)
    private String name;

    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$",
            message = "Kód barvy musí být platný HEX formát začínající mřížkou a obsahující 3 nebo 6 znaků.")
    @Column(name = "hex_color")
    private String hexColor;

    // OPRAVA: Přidáno @Builder.Default, aby builder neignoroval výchozí hodnotu 1
    @Builder.Default
    @Column(name = "sort_order")
    private Integer sortOrder = 1;

    // OPRAVA: Přidáno @Builder.Default, aby builder neignoroval výchozí hodnotu true
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Station> stations = new ArrayList<>();

    // --- STANDARDNÍ METODY ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MainCategory that)) return false;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    /**
     * OPRAVA: Metoda vrácena zpět.
     * FacilityManagementServiceImpl ji vyžaduje a Lombok ji sám v tomto tvaru nevygeneruje.
     */
    public void setActive(boolean active) {
        this.isActive = active;
    }
}