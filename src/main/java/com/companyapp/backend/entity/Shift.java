package com.companyapp.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize; // PŘIDÁNO: Pro optimalizaci N+1

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "shifts")
@Getter
@Setter
@NoArgsConstructor
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Oboustranná vazba na Station. LAZY zajišťuje, že se stanice nenačte
     * automaticky, pokud ji nepotřebujeme.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    /**
     * Oboustranná vazba na ShiftTemplate.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private ShiftTemplate template;

    /**
     * FÁZE 2: Kolekce přiřazení pro EntityGraph a mitigaci N+1 problému.
     * LAZY je zde kritické pro výkon. BatchSize(20) zajistí, že Hibernate
     * načte účastníky pro více směn najednou jedním dotazem.
     */
    @OneToMany(mappedBy = "shift", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @BatchSize(size = 20)
    private List<ShiftAssignment> assignments = new ArrayList<>();

    @NotNull
    @Column(name = "shift_date", nullable = false)
    private LocalDate shiftDate;

    @NotNull
    @Column(name = "start_time", nullable = false)
    private ZonedDateTime startTime;

    @NotNull
    @Column(name = "end_time", nullable = false)
    private ZonedDateTime endTime;

    @Column(name = "required_capacity", nullable = false)
    private Integer requiredCapacity;

    @Column(name = "description", length = 255)
    private String description;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Shift)) return false;
        Shift that = (Shift) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}