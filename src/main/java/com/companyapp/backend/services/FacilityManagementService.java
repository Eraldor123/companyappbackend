package com.companyapp.backend.services;

import java.util.UUID;


public interface FacilityManagementService {
    void deactivateStation(Integer stationId);
    void deactivateMainCategory(Integer categoryId);
}