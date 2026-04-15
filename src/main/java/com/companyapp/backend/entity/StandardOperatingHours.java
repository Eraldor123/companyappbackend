package com.companyapp.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Entity
@Table(name = "standard_operating_hours")
@Getter
@Setter
@NoArgsConstructor
public class StandardOperatingHours {

    @Id
    /*
     * FÁZE 2: Optimalizace transakcí u generátorů ID.
     * OPRAVA: Odstraněno redundantní allocationSize=50 (výchozí hodnota).
     */
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "standard_hours_seq")
    @SequenceGenerator(
            name = "standard_hours_seq",
            sequenceName = "standard_hours_id_seq"
    )
    private Integer id;

    // Týden (Po-Pá)
    @Column(name = "week_dopo_start")
    private LocalTime weekDopoStart;

    @Column(name = "week_dopo_end")
    private LocalTime weekDopoEnd;

    @Column(name = "week_odpo_start")
    private LocalTime weekOdpoStart;

    @Column(name = "week_odpo_end")
    private LocalTime weekOdpoEnd;

    // Víkend (So-Ne)
    /**
     * OPRAVA: Odstraněno redundantní výchozí přiřazení false.
     */
    @Column(name = "weekend_same", nullable = false)
    private Boolean weekendSame;

    @Column(name = "weekend_dopo_start")
    private LocalTime weekendDopoStart;

    @Column(name = "weekend_dopo_end")
    private LocalTime weekendDopoEnd;

    @Column(name = "weekend_odpo_start")
    private LocalTime weekendOdpoStart;

    @Column(name = "weekend_odpo_end")
    private LocalTime weekendOdpoEnd;

    /**
     * OPRAVA: Přidána metoda equals s využitím Pattern Matching (Java 16+).
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StandardOperatingHours that)) return false;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}