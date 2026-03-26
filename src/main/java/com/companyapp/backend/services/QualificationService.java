package com.companyapp.backend.services;

import com.companyapp.backend.services.dto.response.EmployeeQualificationDto;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface QualificationService {
    List<EmployeeQualificationDto> getAllEmployeesWithStations();
    void updateUserStations(UUID userId, Set<Integer> stationIds);
    boolean isUserQualifiedForStation(UUID userId, Integer stationId);
}