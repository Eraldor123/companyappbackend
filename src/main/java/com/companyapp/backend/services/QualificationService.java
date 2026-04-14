package com.companyapp.backend.services;

import com.companyapp.backend.services.dto.response.EmployeeQualificationDto;
import org.springframework.data.domain.Page; // PŘIDÁNO
import org.springframework.data.domain.Pageable; // PŘIDÁNO
import java.util.Set;
import java.util.UUID;

public interface QualificationService {
    /**
     * FÁZE 2: Implementace stránkování.
     * Vrací stránkovaný seznam zaměstnanců, což dramaticky snižuje paměťovou náročnost.
     */
    Page<EmployeeQualificationDto> getAllEmployeesWithStations(Pageable pageable);

    void updateUserStations(UUID userId, Set<Integer> stationIds);
    boolean isUserQualifiedForStation(UUID userId, Integer stationId);
}