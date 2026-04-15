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
    /*
     * FÁZE 2: Optimalizace transakcí u generátorů ID.
     * OPRAVA: Odstraněno redundantní allocationSize=50 (výchozí hodnota v JPA).
     */
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pause_rule_seq")
    @SequenceGenerator(
            name = "pause_rule_seq",
            sequenceName = "pause_rule_id_seq"
    )
    private Integer id;

    /**
     * Práh v hodinách, po kterém se má pauza spustit.
     * Například 4.5 znamená, že pauza se spustí po 4 hodinách a 30 minutách práce.
     */
    @Column(name = "trigger_hours", precision = 4, scale = 2)
    private BigDecimal triggerHours;

    /**
     * Délka pauzy v minutách. Například 15 znamená, že pauza bude trvat 15 minut.
     */
    @Column(name = "pause_duration_minutes")
    private Integer pauseDurationMinutes;

    /**
     * OPRAVA java:S6201: Použití Pattern Matching pro instanceof.
     * Proměnná 'that' je deklarována přímo v podmínce, čímž odpadá nutnost explicitního castingu.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        // Sloučení kontroly typu a vytvoření proměnné do jednoho kroku
        if (!(o instanceof PauseRule that)) return false;

        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}