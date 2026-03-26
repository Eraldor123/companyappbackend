package com.companyapp.backend.services;

import com.companyapp.backend.services.dto.response.EmployeeQualificationDto;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface QualificationService {
    List<EmployeeQualificationDto> getAllEmployeesWithQualifications();
    void updateUserQualifications(UUID userId, Set<Integer> stationIds);
    boolean verifyUserQualificationForStation(UUID userId, Integer stationId);
}