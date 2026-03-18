package com.companyapp.backend.repository;

import com.companyapp.backend.entity.Station;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StationRepository extends JpaRepository<Station, Integer> {

    @EntityGraph(attributePaths = {"category", "reqQualification"})
    List<Station> findByIsActiveTrue();

    <T> ScopedValue<Station> findById(UUID stationId);
}
