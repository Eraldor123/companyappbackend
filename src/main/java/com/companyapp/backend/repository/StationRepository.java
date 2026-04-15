package com.companyapp.backend.repository;

import com.companyapp.backend.entity.Station;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StationRepository extends JpaRepository<Station, Integer> {

    /**
     * OPRAVA java:S1144: Metoda označena SuppressWarnings.
     * Je nezbytná pro budoucí frontendové komponenty (např. výběr stanoviště v plánovači),
     * kde je vyžadován seznam pouze aktuálně aktivních pracovišť.
     * EntityGraph zajišťuje, že se k nim rovnou načtou i jejich kategorie bez N+1 problému.
     */
    @SuppressWarnings("unused")
    @EntityGraph(attributePaths = {"category"})
    List<Station> findByIsActiveTrue();
}