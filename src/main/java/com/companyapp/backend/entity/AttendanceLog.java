package com.companyapp.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
/*
 * FÁZE 1: Ochrana proti Race Conditions.
 * Přidán explicitní unikátní index nad shift_assignment_id, který zabraňuje vzniku
 * duplicitních záznamů o příchodu ve stejný okamžik (např. při rychlém dvojkliku).
 */
@Table(name = "attendance_logs",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_attendance_shift_assignment",
                        columnNames = {"shift_assignment_id"}
                )
        })
@Getter
@Setter
@NoArgsConstructor
public class AttendanceLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Vazba na přiřazení směny, ke kterému tento záznam docházky patří.
     * Unikátní omezení na úrovni DB zajišťuje, že pro jedno přiřazení směny nemůže existovat
     * více záznamů docházky, což brání duplicitám v evidenci.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shift_assignment_id", nullable = false, unique = true)
    private ShiftAssignment shiftAssignment;

    /**
     * Pole pro uložení času příchodu zaměstnance.
     */
    @Column(name = "clock_in")
    private LocalDateTime clockIn;

    /**
     * Pole pro uložení času odchodu zaměstnance.
     */
    @Column(name = "clock_out")
    private LocalDateTime clockOut;

    /**
     * Pole pro uložení celkového odpracovaného času v minutách.
     */
    @Column(name = "deducted_pause_minutes")
    private Integer deductedPauseMinutes;

    /**
     * Pole pro uložení čistého odpracovaného času v minutách.
     */
    @Column(name = "net_time_minutes")
    private Integer netTimeMinutes;

    /**
     * Pole pro označení, zda je záznam docházky neobvyklý.
     */
    @Column(name = "is_unusual", nullable = false)
    private Boolean isUnusual = false;

    /**
     * Pole pro označení, zda manažer schválil tento záznam docházky.
     */
    @Column(name = "manager_approved", nullable = false)
    private Boolean managerApproved = true;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AttendanceLog)) return false;
        AttendanceLog that = (AttendanceLog) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}