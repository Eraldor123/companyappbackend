package com.companyapp.backend.repository;

import com.companyapp.backend.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    /**
     * NOVÉ: Dynamické filtrování (Stránkování + Fulltext hledání + Filtrování modulů).
     * Ošetřeno proti SQL Injection a proti PostgreSQL chybě s typem 'bytea'.
     */
    @Query("SELECT a FROM AuditLog a " +
            "WHERE (:search = '' OR LOWER(a.action) LIKE CONCAT('%', :search, '%') " +
            "                    OR LOWER(a.performedBy) LIKE CONCAT('%', :search, '%') " +
            "                    OR LOWER(a.details) LIKE CONCAT('%', :search, '%')) " +
            "AND (:module = '' OR a.entityName = :module)")
    Page<AuditLog> findFilteredLogs(
            @Param("search") String search,
            @Param("module") String module,
            Pageable pageable
    );
}
