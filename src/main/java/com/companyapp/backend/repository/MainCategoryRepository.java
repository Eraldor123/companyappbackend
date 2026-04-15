package com.companyapp.backend.repository;

import com.companyapp.backend.entity.MainCategory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MainCategoryRepository extends JpaRepository<MainCategory, Integer> {
    @EntityGraph(attributePaths = {"stations", "stations.templates"})
    @Query("SELECT c FROM MainCategory c ORDER BY c.sortOrder ASC")
    List<MainCategory> findAllWithHierarchy();
}

