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
@AllArgsConstructor
@Builder
public class Station {

    /**
     * FÁZE 2: Optimalizace transakcí u generátorů ID.
     * Změna z IDENTITY na SEQUENCE umožňuje Hibernate 6 používat dávkové vkládání.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "station_seq")
    @SequenceGenerator(
            name = "station_seq",
            sequenceName = "station_id_seq"
            // OPRAVA: allocationSize = 50 odstraněno (výchozí hodnota)
    )
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private MainCategory category;

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "capacity_limit")
    private Integer capacityLimit;

    // OPRAVA: Doplněno @Builder.Default pro správnou funkci Builderu
    @Builder.Default
    @Column(name = "needs_qualification", nullable = false)
    private Boolean needsQualification = false;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Builder.Default
    @Column(name = "sort_order")
    private Integer sortOrder = 1;

    @OneToMany(mappedBy = "station", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ShiftTemplate> templates = new ArrayList<>();

    // OPRAVA: Metody addTemplate a removeTemplate odstraněny (nikde nepoužity)

    // --- STANDARDNÍ METODY ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        // OPRAVA: Pattern Matching pro instanceof
        if (!(o instanceof Station that)) return false;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    /**
     * Ponecháno pro kompatibilitu se servisy (např. FacilityManagementServiceImpl).
     */
    public void setActive(boolean b) {
        this.isActive = b;
    }
}