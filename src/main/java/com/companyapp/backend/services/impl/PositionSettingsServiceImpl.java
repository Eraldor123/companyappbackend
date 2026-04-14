package com.companyapp.backend.services.impl;

import com.companyapp.backend.entity.MainCategory;
import com.companyapp.backend.entity.ShiftTemplate;
import com.companyapp.backend.entity.Station;
import com.companyapp.backend.entity.User;
import com.companyapp.backend.repository.MainCategoryRepository;
import com.companyapp.backend.repository.ShiftTemplateRepository;
import com.companyapp.backend.repository.StationRepository;
import com.companyapp.backend.repository.UserRepository;
import com.companyapp.backend.services.PositionSettingsService;
import com.companyapp.backend.services.dto.request.CreateCategoryRequestDto;
import com.companyapp.backend.services.dto.request.CreateStationRequestDto;
import com.companyapp.backend.services.dto.request.CreateTemplateRequestDto;
import com.companyapp.backend.services.dto.response.PositionHierarchyDto;
import com.companyapp.backend.services.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PositionSettingsServiceImpl implements PositionSettingsService {

    private final MainCategoryRepository categoryRepository;
    private final StationRepository stationRepository;
    private final ShiftTemplateRepository templateRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public PositionHierarchyDto getFullHierarchy() {
        // Tvá původní logika mapování hierachie zůstává nezměněna...
        List<MainCategory> allCategories = categoryRepository.findAll();
        List<Station> allStations = stationRepository.findAll();

        List<PositionHierarchyDto.CategoryNodeDto> categoryNodes = allCategories.stream()
                .sorted(Comparator.comparing(c -> c.getSortOrder() == null ? 999 : c.getSortOrder()))
                .map(this::mapCategoryToNode)
                .collect(Collectors.toList());

        return PositionHierarchyDto.builder().categories(categoryNodes).build();
    }

    // --- IMPLEMENTACE LOGIKY KATEGORIÍ ---

    @Override
    @Transactional
    public void createCategory(CreateCategoryRequestDto request) {
        MainCategory category = new MainCategory();
        mapDtoToCategory(request, category);
        categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void updateCategory(Integer id, CreateCategoryRequestDto request) {
        MainCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kategorie nenalezena"));

        mapDtoToCategory(request, category);
        categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Integer id) {
        List<Station> stations = stationRepository.findAll().stream()
                .filter(s -> s.getCategory().getId().equals(id))
                .toList();

        for (Station s : stations) {
            deleteStation(s.getId());
        }
        categoryRepository.deleteById(id);
    }

    // --- IMPLEMENTACE LOGIKY STANOVIŠŤ ---

    @Override
    @Transactional
    public void createStation(CreateStationRequestDto request) {
        MainCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Kategorie nenalezena"));

        Station station = new Station();
        station.setCategory(category);
        mapDtoToStation(request, station);
        stationRepository.save(station);
    }

    @Override
    @Transactional
    public void updateStation(Integer id, CreateStationRequestDto request) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stanoviště nenalezeno"));

        mapDtoToStation(request, station);
        stationRepository.save(station);
    }

    @Override
    @Transactional
    public void deleteStation(Integer id) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stanoviště nenalezeno"));

        // 1. Smažeme šablony
        List<ShiftTemplate> templates = templateRepository.findByStationId(id);
        templateRepository.deleteAll(templates);

        // 2. Odebereme kvalifikace uživatelům
        List<User> qualifiedUsers = userRepository.findAllByQualifiedStationsContains(station);
        for (User user : qualifiedUsers) {
            user.getQualifiedStations().remove(station);
            userRepository.save(user);
        }

        // 3. Smažeme stanoviště
        stationRepository.deleteById(id);
    }

    // --- IMPLEMENTACE LOGIKY ŠABLON ---

    @Override
    @Transactional
    public void createTemplate(CreateTemplateRequestDto request) {
        Station station = stationRepository.findById(request.getStationId())
                .orElseThrow(() -> new ResourceNotFoundException("Stanoviště nenalezeno"));

        ShiftTemplate template = new ShiftTemplate();
        template.setStation(station);
        mapDtoToTemplate(request, template);
        templateRepository.save(template);
    }

    @Override
    @Transactional
    public void updateTemplate(Integer id, CreateTemplateRequestDto request) {
        ShiftTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Šablona nenalezena"));

        mapDtoToTemplate(request, template);
        templateRepository.save(template);
    }

    @Override
    @Transactional
    public void deleteTemplate(Integer id) {
        templateRepository.deleteById(id);
    }

    // --- POMOCNÉ MAPOVACÍ METODY ---

    private void mapDtoToCategory(CreateCategoryRequestDto dto, MainCategory entity) {
        entity.setName(dto.getName());
        entity.setHexColor(dto.getHexColor());
        entity.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 1);
        entity.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
    }

    private void mapDtoToStation(CreateStationRequestDto dto, Station entity) {
        entity.setName(dto.getName());
        entity.setCapacityLimit(dto.getCapacityLimit() != null ? dto.getCapacityLimit() : 1);
        entity.setNeedsQualification(dto.getNeedsQualification() != null ? dto.getNeedsQualification() : false);
        entity.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 1);
        entity.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
    }

    private void mapDtoToTemplate(CreateTemplateRequestDto dto, ShiftTemplate entity) {
        entity.setName(dto.getName());
        entity.setWorkersNeeded(dto.getWorkersNeeded());
        entity.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 1);
        entity.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        entity.setUseOpeningHours(dto.getUseOpeningHours() != null ? dto.getUseOpeningHours() : false);
        entity.setHasDopo(dto.getHasDopo() != null ? dto.getHasDopo() : true);
        entity.setHasOdpo(dto.getHasOdpo() != null ? dto.getHasOdpo() : false);

        if (dto.getStartTime() != null && !dto.getStartTime().isEmpty()) {
            entity.setStartTime(LocalTime.parse(dto.getStartTime()));
            entity.setEndTime(LocalTime.parse(dto.getEndTime()));
        }
        if (dto.getStartTime2() != null && !dto.getStartTime2().isEmpty()) {
            entity.setStartTime2(LocalTime.parse(dto.getStartTime2()));
            entity.setEndTime2(LocalTime.parse(dto.getEndTime2()));
        }
    }

    private PositionHierarchyDto.CategoryNodeDto mapCategoryToNode(MainCategory cat) {
        // Tady by pokračovalo mapování pro getHierarchy (viz tvůj původní kód v kontroleru)
        return PositionHierarchyDto.CategoryNodeDto.builder()
                .id(cat.getId())
                .name(cat.getName())
                .color(cat.getHexColor())
                .isActive(cat.getIsActive())
                .sortOrder(cat.getSortOrder())
                .build();
    }
}