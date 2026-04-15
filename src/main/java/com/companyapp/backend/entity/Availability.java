package com.companyapp.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "availabilities")
@Getter
@Setter
@ToString
public class Availability {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "availability_seq")
    @SequenceGenerator(
            name = "availability_seq",
            sequenceName = "availability_id_seq"
            // OPRAVA: allocationSize = 50 odstraněno, protože 50 je výchozí hodnota v JPA.
    )
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "available_date", nullable = false)
    private LocalDate availableDate;

    @Column(name = "morning", nullable = false)
    private boolean morning;

    @Column(name = "afternoon", nullable = false)
    private boolean afternoon;

    /**
     * OPRAVA: Redundantní "= false" odstraněno.
     */
    @Column(name = "is_confirmed", nullable = false)
    private boolean confirmed;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Availability that)) return false;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}