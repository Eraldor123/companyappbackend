package com.companyapp.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "pause_rules")
@Getter
@Setter
@NoArgsConstructor
public class PauseRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Práh v hodinách, po kterém se má pauza spustit. Například 4.5 znamená, že pauza se spustí po 4 hodinách a 30 minutách práce.
     */
    @Column(name = "trigger_hours", precision = 4, scale = 2)
    private BigDecimal triggerHours;

    /**
     * Délka pauzy v minutách. Například 15 znamená, že pauza bude trvat 15 minut.
     */
    @Column(name = "pause_duration_minutes")
    private Integer pauseDurationMinutes;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PauseRule)) return false;
        PauseRule that = (PauseRule) o;
        return id!= null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
