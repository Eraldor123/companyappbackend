package com.companyapp.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Entity
@Table(name = "shift_templates")
@Getter
@Setter
@NoArgsConstructor
public class ShiftTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "workers_needed")
    private Integer workersNeeded;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "start_time_2")
    private LocalTime startTime2;

    @Column(name = "end_time_2")
    private LocalTime endTime2;

    // --- PŘIDÁNO: Aktivní stav ---
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShiftTemplate)) return false;
        ShiftTemplate that = (ShiftTemplate) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public void setActive(boolean b) {
        this.isActive = b;
    }
}