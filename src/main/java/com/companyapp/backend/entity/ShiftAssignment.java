package com.companyapp.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
        name = "shift_assignments",
        uniqueConstraints = {
                // Opraveno na 'employee_id', aby to odpovídalo poli v Javě
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
    private java.time.LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private java.time.LocalDateTime endTime;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift shift;

    /**
     * Změněno z 'user' na 'employee', aby zmizely červené chyby v Repository.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private User employee;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShiftAssignment)) return false;
        ShiftAssignment that = (ShiftAssignment) o;
        return id!= null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}