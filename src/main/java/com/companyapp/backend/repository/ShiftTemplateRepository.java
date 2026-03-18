package com.companyapp.backend.repository;

import com.companyapp.backend.entity.ShiftTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShiftTemplateRepository extends JpaRepository<ShiftTemplate, Integer> {
    List<ShiftTemplate> findByStationId(Integer stationId);
}
