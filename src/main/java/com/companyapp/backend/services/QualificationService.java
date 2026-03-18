package com.companyapp.backend.services;

import java.util.Set;
import java.util.UUID;

public interface QualificationService {
    void updateUserQualifications(UUID userId, Set<UUID> qualificationIds);
    boolean verifyUserQualificationForStation(UUID userId, Integer stationId);
}