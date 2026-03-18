package com.companyapp.backend.entity;

import com.companyapp.backend.enums.AvailabilityStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "availabilities",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_date_avail", columnNames = {"user_id", "available_date"})
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Availability {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Vazba na uživatele, kterému tato dostupnost patří. Každá dostupnost musí být přiřazena k jednomu uživateli, což zajišťuje, že každá dostupnost je jednoznačně spojená s konkrétním zaměstnancem. Tato vazba je povinná, protože každá dostupnost musí být přiřazena k nějakému uživateli.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Pole pro uložení data, ke kterému se vztahuje dostupnost. Toto pole je povinné, protože každá dostupnost musí být spojena s konkrétním datem, aby bylo možné správně plánovat a organizovat směny a další aktivity. Unikátní omezení zajišťuje, že pro jednoho uživatele nemůže existovat více záznamů dostupnosti pro stejné datum, což pomáhá udržovat konzistenci dat a zabraňuje duplicitám v plánování.
     */
    @NotNull
    @Column(name = "available_date", nullable = false)
    private LocalDate availableDate;

    /**
     * Pole pro uložení stavu dostupnosti uživatele pro dané datum. Tento enum umožňuje snadné kategorizování a filtrování dostupností podle jejich stavu (např. k dispozici, nedostupný, na dovolené atd.). Toto pole je povinné, protože každý záznam dostupnosti musí mít definovaný stav, který určuje, zda je uživatel k dispozici nebo ne pro dané datum.
     */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "status", columnDefinition = "availability_status", nullable = false)
    private AvailabilityStatus status;

    /**
     * Pole pro označení, zda je uživatel k dispozici ráno. Toto pole je povinné a musí být vyplněno. Umožňuje detailnější specifikaci dostupnosti uživatele během dne, což může být užitečné pro plánování směn a dalších aktivit.
     */
    @Column(name = "morning", nullable = false)
    private Boolean morning;

    /**
     * Pole pro označení, zda je uživatel k dispozici odpoledne. Toto pole je povinné a musí být vyplněno. Umožňuje detailnější specifikaci dostupnosti uživatele během dne, což může být užitečné pro plánování směn a dalších aktivit.
     */
    @Column(name = "afternoon", nullable = false)
    private Boolean afternoon;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Availability)) return false;
        Availability that = (Availability) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}