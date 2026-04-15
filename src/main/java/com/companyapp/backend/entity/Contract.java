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
     * Vazba na uživatele, kterému tento kontrakt patří.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Typ kontraktu (např. DPP, HPP).
     */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "type", nullable = false)
    private ContractType type;

    @Column(name = "hourly_wage", precision = 10, scale = 2)
    private BigDecimal hourlyWage;

    @Column(name = "monthly_wage", precision = 10, scale = 2)
    private BigDecimal monthlyWage;

    @Column(name = "fte", precision = 4, scale = 2)
    private BigDecimal fte;

    @Column(name = "company_id_number")
    private String companyIdNumber;

    @NotNull
    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    /**
     * OPRAVA java:S6201: Implementace Pattern Matching pro instanceof.
     * Zpřehledňuje kód a odstraňuje nutnost explicitního přetypování na novém řádku.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        // Proměnná 'that' je deklarována přímo v podmínce
        if (!(o instanceof Contract that)) return false;

        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}