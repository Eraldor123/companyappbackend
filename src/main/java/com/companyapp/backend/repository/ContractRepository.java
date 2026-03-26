package com.companyapp.backend.repository;

import com.companyapp.backend.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ContractRepository extends JpaRepository<Contract, UUID> {

    @Query("SELECT c FROM Contract c WHERE c.user.id = :userId " +
            "AND c.validFrom <= :targetDate " +
            "AND (c.validTo IS NULL OR c.validTo >= :targetDate)")
    List<Contract> findActiveContractsForUserAtDate(
            @Param("userId") UUID userId,
            @Param("targetDate") LocalDate targetDate);

    @Query("SELECT c FROM Contract c WHERE c.user.id = :userId ORDER BY c.validFrom DESC LIMIT 1")
    java.util.Optional<Contract> findLatestContractByUserId(@Param("userId") UUID userId);
}