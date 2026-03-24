package com.companyapp.backend.services;

import com.companyapp.backend.services.dto.response.PositionHierarchyDto;

public interface PositionSettingsService {
    PositionHierarchyDto getFullHierarchy();
}
