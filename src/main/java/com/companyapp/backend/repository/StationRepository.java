package com.companyapp.backend.repository;

import com.companyapp.backend.entity.Station;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StationRepository extends JpaRepository<Station, Integer> {
    @EntityGraph(attributePaths = {"category"})
    List<Station> findByIsActiveTrue();
}