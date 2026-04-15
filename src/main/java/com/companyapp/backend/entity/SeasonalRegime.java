package com.companyapp.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "seasonal_regimes")
@Getter
@Setter
@NoArgsConstructor
public class SeasonalRegime {

    @Id
    /*
     * FÁZE 2: Optimalizace transakcí u generátorů ID.
     * OPRAVA: Odstraněno redundantní allocationSize=50 (výchozí hodnota).
     */
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seasonal_regime_seq")
    @SequenceGenerator(
            name = "seasonal_regime_seq",
            sequenceName = "seasonal_regime_id_seq"
    )
    private Integer id;

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "dopo_start")
    private LocalTime dopoStart;

    @Column(name = "dopo_end")
    private LocalTime dopoEnd;

    @Column(name = "odpo_start")
    private LocalTime odpoStart;

    @Column(name = "odpo_end")
    private LocalTime odpoEnd;

    /**
     * OPRAVA: Přidána standardní metoda equals s využitím Pattern Matching (Java 16+).
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SeasonalRegime that)) return false;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}