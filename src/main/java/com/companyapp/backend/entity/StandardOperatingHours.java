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
    /**
     * FÁZE 2: Optimalizace transakcí u generátorů ID.
     * Změna z IDENTITY na SEQUENCE pro podporu batchingu v Hibernate 6.
     * allocationSize = 50 umožňuje aplikaci rezervovat si blok ID v paměti a šetřit dotazy do DB.
     */
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "standard_hours_seq")
    @SequenceGenerator(
            name = "standard_hours_seq",
            sequenceName = "standard_hours_id_seq",
            allocationSize = 50
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
    @Column(name = "weekend_same", nullable = false)
    private Boolean weekendSame = false;

    @Column(name = "weekend_dopo_start")
    private LocalTime weekendDopoStart;

    @Column(name = "weekend_dopo_end")
    private LocalTime weekendDopoEnd;

    @Column(name = "weekend_odpo_start")
    private LocalTime weekendOdpoStart;

    @Column(name = "weekend_odpo_end")
    private LocalTime weekendOdpoEnd;
}