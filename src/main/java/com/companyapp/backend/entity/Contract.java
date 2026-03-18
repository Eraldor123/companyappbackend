package com.companyapp.backend.entity;

import com.companyapp.backend.enums.ContractType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "contracts")
@Getter
@Setter
@NoArgsConstructor
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Vazba na uživatele, kterému tento kontrakt patří. Každý kontrakt musí být přiřazen k jednomu uživateli, což zajišťuje, že každý kontrakt je jednoznačně spojen s konkrétním zaměstnancem. Tato vazba je povinná, protože každý kontrakt musí být přiřazen k nějakému uživateli.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Pole pro uložení typu kontraktu, který určuje, jaký druh pracovního vztahu tento kontrakt představuje (např. plný úvazek, částečný úvazek, dočasný kontrakt atd.). Tento enum umožňuje snadné kategorizování a filtrování kontraktů podle jejich typu. Toto pole je povinné, protože každý kontrakt musí mít definovaný typ.
     */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "type", nullable = false)
    private ContractType type;

    /**
     * Pole pro uložení hodinové mzdy. Toto pole je volitelné, protože některé kontrakty mohou být založeny na měsíční mzdě nebo jiných podmínkách, a nemusí tedy mít definovanou hodinovou mzdu.
     */
    @Column(name = "hourly_wage", precision = 10, scale = 2)
    private BigDecimal hourlyWage;

    /**
     * Pole pro uložení měsíční mzdy. Toto pole je volitelné, protože některé kontrakty mohou být založeny na hodinové mzdě nebo jiných podmínkách, a nemusí tedy mít definovanou měsíční mzdu.
     */
    @Column(name = "monthly_wage", precision = 10, scale = 2)
    private BigDecimal monthlyWage;

    /**
     * Pole pro uložení FTE (Full-Time Equivalent), které udává, jaký podíl plného úvazku tento kontrakt představuje. Například FTE 1.0 znamená plný úvazek, FTE 0.5 znamená poloviční úvazek atd. Toto pole je volitelné, protože některé kontrakty nemusí mít definovaný FTE, zejména pokud jsou založeny na jiných podmínkách než na úvazku.
     */
    @Column(name = "fte", precision = 4, scale = 2)
    private BigDecimal fte;

    /**
     * Pole pro uložení identifikačního čísla společnosti, které může být použito pro různé účely, jako je například propojení s externími systémy, identifikace kontraktu v rámci společnosti nebo pro interní evidenci. Toto pole je volitelné, protože ne všechny kontrakty musí mít definované identifikační číslo společnosti.
     */
    @Column(name = "company_id_number")
    private String companyIdNumber;

    /**
     * Pole pro uložení data, od kterého je tento kontrakt platný. Toto pole je povinné, protože každý kontrakt musí mít datum začátku platnosti.
     */
    @NotNull
    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    /**
     * Pole pro uložení data, do kterého je tento kontrakt platný. Toto pole je volitelné, protože některé kontrakty mohou být platné na dobu neurčitou a nemusí mít definované datum konce platnosti.
     */
    @Column(name = "valid_to")
    private LocalDate validTo;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Contract)) return false;
        Contract that = (Contract) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}