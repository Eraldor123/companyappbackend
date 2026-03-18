package com.companyapp.backend.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "attendance_logs")
@Getter
@Setter
@NoArgsConstructor
public class AttendanceLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Vazba na přiřazení směny, ke kterému tento záznam docházky patří. Každý záznam docházky musí být přiřazen k jednomu přiřazení směny, což zajišťuje, že každý záznam docházky je jednoznačně spojen s konkrétním přiřazením směny. Tato vazba je povinná, protože každý záznam docházky musí být přiřazen k nějakému přiřazení směny, aby bylo možné správně sledovat a organizovat docházku zaměstnanců. Unikátní omezení zajišťuje, že pro jedno přiřazení směny nemůže existovat více záznamů docházky, což pomáhá udržovat konzistenci dat a zabraňuje duplicitám v evidenci docházky.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shift_assignment_id", nullable = false, unique = true)
    private ShiftAssignment shiftAssignment;

    /**
     * Pole pro uložení času příchodu zaměstnance. Toto pole je povinné, protože každý záznam docházky musí mít definovaný čas příchodu, aby bylo možné správně sledovat a organizovat docházku zaměstnanců. Ukládáme čas příchodu jako LocalDateTime, což umožňuje snadné výpočty odpracovaného času a další analýzy.
     */
    @Column(name = "clock_in")
    private LocalDateTime clockIn;

    /**
     * Pole pro uložení času odchodu zaměstnance. Toto pole je povinné, protože každý záznam docházky musí mít definovaný čas odchodu, aby bylo možné správně sledovat a organizovat docházku zaměstnanců. Ukládáme čas odchodu jako LocalDateTime, což umožňuje snadné výpočty odpracovaného času a další analýzy.
     */
    @Column(name = "clock_out")
    private LocalDateTime clockOut;

    /**
     * Pole pro uložení celkového odpracovaného času v minutách. Toto pole je povinné, protože každý záznam docházky musí mít definovaný odpracovaný čas, aby bylo možné správně sledovat a organizovat docházku zaměstnanců. Ukládáme odpracovaný čas v minutách, což umožňuje snadné výpočty a reportování.
     */
    @Column(name = "deducted_pause_minutes")
    private Integer deductedPauseMinutes;

    /**
     * Pole pro uložení čistého odpracovaného času v minutách (odpracovaný čas minus odečtené pauzy). Toto pole je povinné, protože každý záznam docházky musí mít definovaný čistý odpracovaný čas, aby bylo možné správně sledovat a organizovat docházku zaměstnanců. Ukládáme čistý odpracovaný čas v minutách, což umožňuje snadné výpočty a reportování.
     */
    @Column(name = "net_time_minutes")
    private Integer netTimeMinutes;

    /**
     * Pole pro označení, zda je záznam docházky neobvyklý (např. pozdní příchod, brzy odchod, dlouhá pauza atd.). Toto pole je povinné a výchozí hodnota je false, což znamená, že záznam není považován za neobvyklý, pokud není explicitně označen jako takový. Toto pole umožňuje snadné identifikování a filtrování záznamů docházky, které vyžadují pozornost manažera nebo další akce.
     */
    @Column(name = "is_unusual", nullable = false)
    private Boolean isUnusual = false;

    /**
     * Pole pro označení, zda manažer schválil tento záznam docházky. Toto pole je povinné a výchozí hodnota je true, což znamená, že záznam je považován za schválený, pokud není explicitně označen jako neschválený. Toto pole umožňuje snadné identifikování a filtrování záznamů docházky, které byly schváleny nebo neschváleny manažerem, což může být důležité pro správu docházky a řešení případných problémů s docházkou zaměstnanců.
     */
    @Column(name = "manager_approved", nullable = false)
    private Boolean managerApproved = true;

    /**
     * Pole pro uložení poznámky manažera k tomuto záznamu docházky. Toto pole je volitelné, protože ne všechny záznamy docházky budou mít poznámku od manažera. Toto pole umožňuje manažerům přidávat důležité informace nebo komentáře k záznamům docházky, které mohou být užitečné pro budoucí reference nebo pro řešení případných problémů s docházkou zaměstnanců.
     * param object
      * @return true pokud jsou objekty stejné, false jinak
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AttendanceLog)) return false;
        AttendanceLog that = (AttendanceLog) o;
        return id!= null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
