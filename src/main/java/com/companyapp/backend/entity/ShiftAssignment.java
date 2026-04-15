package com.companyapp.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "shift_assignments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_shift_employee", columnNames = {"shift_id", "employee_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
public class ShiftAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift shift;

    /**
     * Vazba na zaměstnance.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private User employee;

    /**
     * OPRAVA java:S6201: Použití Pattern Matching pro instanceof.
     * Proměnná 'that' je definována přímo v podmínce, což zpřehledňuje kód.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        // Kontrola typu a deklarace proměnné 'that' v jednom kroku
        if (!(o instanceof ShiftAssignment that)) return false;

        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}