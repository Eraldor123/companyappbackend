package com.companyapp.backend.repository;

import com.companyapp.backend.entity.MainCategory;
import com.companyapp.backend.entity.Station;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MainCategoryRepository extends JpaRepository<MainCategory, Integer> {
    List<MainCategory> findByIsActiveTrue();
}

