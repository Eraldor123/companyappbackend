package com.companyapp.backend.repository;

import com.companyapp.backend.entity.StandardOperatingHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StandardOperatingHoursRepository extends JpaRepository<StandardOperatingHours, Integer> {
}
