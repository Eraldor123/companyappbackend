package com.companyapp.backend.services;

import com.companyapp.backend.services.dto.request.CreateCategoryRequestDto;
import com.companyapp.backend.services.dto.request.CreateStationRequestDto;
import com.companyapp.backend.services.dto.request.CreateTemplateRequestDto;
import com.companyapp.backend.services.dto.response.PositionHierarchyDto;

public interface PositionSettingsService {
    PositionHierarchyDto getFullHierarchy();

    void createCategory(CreateCategoryRequestDto request);
    void updateCategory(Integer id, CreateCategoryRequestDto request);
    void deleteCategory(Integer id);

    void createStation(CreateStationRequestDto request);
    void updateStation(Integer id, CreateStationRequestDto request);
    void deleteStation(Integer id);

    void createTemplate(CreateTemplateRequestDto request);
    void updateTemplate(Integer id, CreateTemplateRequestDto request);
    void deleteTemplate(Integer id);
}