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
     * více záznamů docházky.
     */
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

    /**
     * OPRAVA java:S6201: Použití Pattern Matching pro instanceof.
     * Proměnná 'that' se definuje přímo v podmínce, čímž odpadá explicitní přetypování.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        // Sloučení kontroly typu a vytvoření proměnné 'that' do jednoho řádku
        if (!(o instanceof AttendanceLog that)) return false;

        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}