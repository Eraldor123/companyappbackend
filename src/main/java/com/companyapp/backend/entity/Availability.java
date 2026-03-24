package com.companyapp.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID; // Přidán import pro UUID

@Entity
@Table(name = "availabilities")
@Data
public class Availability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId; // TADY JE OPRAVA z Long na UUID

    @Column(name = "available_date", nullable = false)
    private LocalDate availableDate;

    @Column(name = "morning", nullable = false)
    private boolean morning;

    @Column(name = "afternoon", nullable = false)
    private boolean afternoon;

    @Column(name = "is_confirmed", nullable = false)
    private boolean confirmed = false;
}