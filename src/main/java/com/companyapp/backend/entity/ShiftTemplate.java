package com.companyapp.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "shift_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shift_template_seq")
    @SequenceGenerator(
            name = "shift_template_seq",
            sequenceName = "shift_template_id_seq"
            // OPRAVA: allocationSize = 50 odstraněno (výchozí hodnota v JPA)
    )
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

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 1;

    @Column(name = "use_opening_hours")
    @Builder.Default
    private Boolean useOpeningHours = false;

    @Column(name = "has_dopo")
    @Builder.Default
    private Boolean hasDopo = true;

    @Column(name = "has_odpo")
    @Builder.Default
    private Boolean hasOdpo = false;

    /**
     * OPRAVA java:S6201: Použití Pattern Matching pro instanceof.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        // Proměnná 'that' je deklarována přímo v podmínce
        if (!(o instanceof ShiftTemplate that)) return false;

        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    /**
     * Ponecháno pro kompatibilitu se servisy (např. FacilityManagementService).
     */
    public void setActive(boolean b) {
        this.isActive = b;
    }
}