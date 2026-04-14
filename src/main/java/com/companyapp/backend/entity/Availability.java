package com.companyapp.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "availabilities")
@Data
public class Availability {

    @Id
    /**
     * FÁZE 2: Optimalizace ID generátoru pro hromadné operace.
     * Použití SEQUENCE s allocationSize=50 umožňuje Hibernate 6 efektivní batching.
     * Tím se eliminuje "N+1" dotazování do databáze pro každé nové ID.
     */
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "availability_seq")
    @SequenceGenerator(
            name = "availability_seq",
            sequenceName = "availability_id_seq",
            allocationSize = 50
    )
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "available_date", nullable = false)
    private LocalDate availableDate;

    @Column(name = "morning", nullable = false)
    private boolean morning;

    @Column(name = "afternoon", nullable = false)
    private boolean afternoon;

    @Column(name = "is_confirmed", nullable = false)
    private boolean confirmed = false;
}