package com.companyapp.backend.services;

import com.companyapp.backend.services.dto.response.EmployeeQualificationDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Set;
import java.util.UUID;

public interface QualificationService {

    /**
     * Původní metoda pouze se stránkováním (bez filtrů).
     */
    Page<EmployeeQualificationDto> getAllEmployeesWithStations(Pageable pageable);

    /**
     * NOVÁ METODA: Stránkování doplněné o vyhledávání podle jména a filtru úvazku.
     * @param search Volitelný hledaný text (jméno/příjmení)
     * @param contractType Volitelný typ úvazku (např. "HPP", "VŠE")
     */
    Page<EmployeeQualificationDto> getAllEmployeesWithStationsFiltered(Pageable pageable, String search, String contractType);

    void updateUserStations(UUID userId, Set<Integer> stationIds);

    boolean isUserQualifiedForStation(UUID userId, Integer stationId);
}