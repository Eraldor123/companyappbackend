package com.companyapp.backend.services;

import java.util.UUID;

public interface FacilityManagementService {
    // Pro ukázku zjednodušeno, v budoucnu zde přibudou DTO pro MainCategory a ShiftTemplate
    void deactivateStation(UUID stationId);
    void deactivateMainCategory(UUID categoryId);
}