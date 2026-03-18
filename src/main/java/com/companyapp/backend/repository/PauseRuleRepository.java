package com.companyapp.backend.repository;

import com.companyapp.backend.entity.PauseRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PauseRuleRepository extends JpaRepository<PauseRule, Integer> {
    List<PauseRule> findAllByOrderByTriggerHoursAsc();
}