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

    // Tato magická metoda nám později ve Fázi 4 najde, jestli pro zadaný den platí nějaká sezóna!
    @Query("SELECT s FROM SeasonalRegime s WHERE :targetDate BETWEEN s.startDate AND s.endDate")
    List<SeasonalRegime> findActiveRegimesForDate(@Param("targetDate") LocalDate targetDate);
}
