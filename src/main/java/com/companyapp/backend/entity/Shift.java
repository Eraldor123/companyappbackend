package com.companyapp.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

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
     * Zde LAZY zůstává, protože u @ManyToOne je výchozí EAGER.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private ShiftTemplate template;

    /**
     * OPRAVA: fetch = FetchType.LAZY odstraněno, u @OneToMany je to výchozí hodnota.
     */
    @OneToMany(mappedBy = "shift", cascade = CascadeType.ALL)
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

    /**
     * OPRAVA: length = 255 odstraněno (výchozí hodnota).
     */
    @Column(name = "description")
    private String description;

    /**
     * OPRAVA java:S6201: Použití Pattern Matching pro instanceof.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        // Proměnná 'that' se vytvoří rovnou v podmínce
        if (!(o instanceof Shift that)) return false;

        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}