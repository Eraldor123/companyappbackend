package com.companyapp.backend.repository;

import com.companyapp.backend.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContractRepository extends JpaRepository<Contract, UUID> {

    /**
     * OPRAVA java:S1144: Metoda označena SuppressWarnings, protože je nezbytná
     * pro výpočet mezd a validaci plánování k určitému historickému nebo budoucímu datu.
     */
    @SuppressWarnings("unused")
    @Query("SELECT c FROM Contract c WHERE c.user.id = :userId " +
            "AND c.validFrom <= :targetDate " +
            "AND (c.validTo IS NULL OR c.validTo >= :targetDate)")
    List<Contract> findActiveContractsForUserAtDate(
            @Param("userId") UUID userId,
            @Param("targetDate") LocalDate targetDate);

    /**
     * Vyhledá nejnovější smlouvu uživatele.
     * OPRAVA: Odstraněno plné jméno balíčku u Optional pro čistší kód.
     */
    @Query("SELECT c FROM Contract c WHERE c.user.id = :userId ORDER BY c.validFrom DESC LIMIT 1")
    Optional<Contract> findLatestContractByUserId(@Param("userId") UUID userId);
}