package com.companyapp.backend.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
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
     * Oboustranná vazba na Station, která umožňuje získat všechny směny pro danou stanici. Na straně Station je definována jako @OneToMany(mappedBy = "station").
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    /**
     * Oboustranná vazba na ShiftTemplate, která umožňuje získat všechny směny, které jsou založeny na dané šabloně. Na straně ShiftTemplate je definována jako @OneToMany(mappedBy = "template").
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private ShiftTemplate template;

    /**
     * Datum směny. Tento atribut je důležitý pro plánování a zobrazení směn v kalendáři
     */
    @NotNull
    @Column(name = "shift_date", nullable = false)
    private LocalDate shiftDate;

    /**
     * Čas začátku a konce směny. Tyto atributy jsou klíčové pro plánování a zobrazení směn v kalendáři, stejně jako pro výpočet odpracovaných hodin a kontrolu překrývání směn.
     */
    @NotNull
    @Column(name = "start_time", nullable = false)
    private ZonedDateTime startTime;

    /**
     * Čas začátku a konce směny. Tyto atributy jsou klíčové pro plánování a zobrazení směn v kalendáři, stejně jako pro výpočet odpracovaných hodin a kontrolu překrývání směn.
     */
    @NotNull
    @Column(name = "end_time", nullable = false)
    private ZonedDateTime endTime;

    /**
     * Počet pracovníků, kteří jsou potřeba pro tuto směnu. Tento atribut je důležitý pro plánování a zajištění dostatečného personálu pro každou směnu.
     */
    @Column(name = "required_capacity", nullable = false)
    private Integer requiredCapacity;

    @Column(name = "description", length = 255)
    private String description;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Shift)) return false;
        Shift that = (Shift) o;
        return id!= null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
