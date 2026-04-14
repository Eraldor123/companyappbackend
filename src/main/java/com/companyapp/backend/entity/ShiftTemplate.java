package com.companyapp.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Entity
@Table(name = "shift_templates")
@Getter
@Setter
@NoArgsConstructor
public class ShiftTemplate {

    @Id
    /**
     * FÁZE 2: Optimalizace transakcí u generátorů ID.
     * Změna z IDENTITY na SEQUENCE pro podporu batchingu v Hibernate 6.
     * allocationSize = 50 zajišťuje, že se ID nepoptávají v DB po jednom, ale berou se z paměťového poolu.
     */
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shift_template_seq")
    @SequenceGenerator(
            name = "shift_template_seq",
            sequenceName = "shift_template_id_seq",
            allocationSize = 50
    )
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "workers_needed")
    private Integer workersNeeded;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "start_time_2")
    private LocalTime startTime2;

    @Column(name = "end_time_2")
    private LocalTime endTime2;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Pořadí pro zobrazení v uživatelském rozhraní.
     */
    @Column(name = "sort_order")
    private Integer sortOrder = 1;

    @Column(name = "use_opening_hours")
    private Boolean useOpeningHours = false;

    @Column(name = "has_dopo")
    private Boolean hasDopo = true;

    @Column(name = "has_odpo")
    private Boolean hasOdpo = false;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShiftTemplate)) return false;
        ShiftTemplate that = (ShiftTemplate) o;
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