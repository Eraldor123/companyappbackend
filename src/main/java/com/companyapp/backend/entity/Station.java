package com.companyapp.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalTime; // PŘIDANÝ IMPORT
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

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "station_seq")
    @SequenceGenerator(
            name = "station_seq",
            sequenceName = "station_id_seq"
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

    // === NOVÝ SLOUPEC PRO TVŮJ MODÁL ===
    // Tady se uloží hodnota "Odpolední směna od" (např. 14:00)
    @Column(name = "afternoon_start_time")
    private LocalTime afternoonStartTime;
    // ===================================

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

    // --- STANDARDNÍ METODY ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Station that)) return false;
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