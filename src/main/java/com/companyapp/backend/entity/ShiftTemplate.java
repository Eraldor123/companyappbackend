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

    /**
     * Oboustranná vazba na Station, která umožňuje získat všechny šablony směn pro danou stanici. Na straně Station je definována jako @OneToMany(mappedBy = "station").
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Počet pracovníků potřebných pro tuto šablonu směny. Tento atribut je důležitý pro plánování a zajištění dostatečného počtu pracovníků pro každou směnu založenou na této šabloně.
     */
    @Column(name = "workers_needed")
    private Integer workersNeeded;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShiftTemplate)) return false;
        ShiftTemplate that = (ShiftTemplate) o;
        return id!= null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
