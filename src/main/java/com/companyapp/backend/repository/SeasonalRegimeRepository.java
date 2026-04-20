package com.companyapp.backend.repository;

import com.companyapp.backend.entity.SeasonalRegime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SeasonalRegimeRepository extends JpaRepository<SeasonalRegime, Integer> {

    // --- OPRAVENO: Přidáno "AND s.isActive = true" ---
    @Query("SELECT s FROM SeasonalRegime s WHERE :targetDate BETWEEN s.startDate AND s.endDate AND s.isActive = true")
    List<SeasonalRegime> findActiveRegimesForDate(@Param("targetDate") LocalDate targetDate);
}