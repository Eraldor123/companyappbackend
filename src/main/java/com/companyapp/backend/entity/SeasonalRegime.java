package com.companyapp.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "seasonal_regimes")
@Getter
@Setter
@NoArgsConstructor
public class SeasonalRegime {

    @Id
    /**
     * FÁZE 2: Optimalizace transakcí u generátorů ID.
     * Změna z IDENTITY na SEQUENCE pro podporu batchingu v Hibernate 6.
     * allocationSize = 50 zajišťuje, že se ID nepoptávají v DB po jednom, ale berou se z paměťového poolu.
     */
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seasonal_regime_seq")
    @SequenceGenerator(
            name = "seasonal_regime_seq",
            sequenceName = "seasonal_regime_id_seq",
            allocationSize = 50
    )
    private Integer id;

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "dopo_start")
    private LocalTime dopoStart;

    @Column(name = "dopo_end")
    private LocalTime dopoEnd;

    @Column(name = "odpo_start")
    private LocalTime odpoStart;

    @Column(name = "odpo_end")
    private LocalTime odpoEnd;
}