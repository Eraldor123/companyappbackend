package com.companyapp.backend.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "attendance_logs")
@Getter
@Setter
@NoArgsConstructor
public class AttendanceLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Vazba garantuje, že pro jeden záznam přiřazení existuje unikátní attendance log.
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shift_assignment_id", nullable = false, unique = true)
    private ShiftAssignment shiftAssignment;

    @Column(name = "clock_in")
    private LocalDateTime clockIn;

    @Column(name = "clock_out")
    private LocalDateTime clockOut;

    @Column(name = "deducted_pause_minutes")
    private Integer deductedPauseMinutes;

    @Column(name = "net_time_minutes")
    private Integer netTimeMinutes;

    @Column(name = "is_unusual", nullable = false)
    private Boolean isUnusual = false;

    @Column(name = "manager_approved", nullable = false)
    private Boolean managerApproved = true;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AttendanceLog)) return false;
        AttendanceLog that = (AttendanceLog) o;
        return id!= null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
