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
import java.util.ArrayList;
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
        List<MainCategory> allCategories = categoryRepository.findAll();
        List<PositionHierarchyDto.CategoryNodeDto> categoryNodes = allCategories.stream()
                .sorted(Comparator.comparing(c -> c.getSortOrder() == null ? 999 : c.getSortOrder()))
                .map(this::mapCategoryToNode)
                .collect(Collectors.toList());

        return PositionHierarchyDto.builder().categories(categoryNodes).build();
    }

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
        MainCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kategorie nenalezena"));

        if (category.getStations() != null) {
            new ArrayList<>(category.getStations()).forEach(s -> deleteStation(s.getId()));
        }
        categoryRepository.delete(category);
    }

    @Override
    @Transactional
    public void createStation(CreateStationRequestDto request) {
        MainCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Kategorie nenalezena"));

        Station station = new Station();
        station.setCategory(category);
        mapDtoToStation(request, station);
        Station savedStation = stationRepository.save(station);

        // OPRAVA: Místo setStations použijeme getStations().add()
        if (category.getStations() == null) {
            // Tohle by se nemělo stát, pokud máš v Entitě = new ArrayList<>()
            // ale pro jistotu to tu necháme
        } else {
            category.getStations().add(savedStation);
        }
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

        List<ShiftTemplate> templates = templateRepository.findByStationId(id);
        templateRepository.deleteAll(templates);

        List<User> qualifiedUsers = userRepository.findAllByQualifiedStationsContains(station);
        for (User user : qualifiedUsers) {
            // TADY BYLA CHYBA: Ujisti se, že User má metodu getQualifiedStations()
            user.getQualifiedStations().remove(station);
        }
        userRepository.saveAll(qualifiedUsers);
        stationRepository.delete(station);
    }

    @Override
    @Transactional
    public void createTemplate(CreateTemplateRequestDto request) {
        Station station = stationRepository.findById(request.getStationId())
                .orElseThrow(() -> new ResourceNotFoundException("Stanoviště nenalezeno"));

        ShiftTemplate template = new ShiftTemplate();
        template.setStation(station);
        mapDtoToTemplate(request, template);
        ShiftTemplate savedTemplate = templateRepository.save(template);

        // OPRAVA: Místo setTemplates použijeme getTemplates().add()
        if (station.getTemplates() != null) {
            station.getTemplates().add(savedTemplate);
        }
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

    // --- POMOCNÉ METODY ---

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
        List<PositionHierarchyDto.StationNodeDto> stationNodes = (cat.getStations() != null)
                ? cat.getStations().stream()
                .sorted(Comparator.comparing(s -> s.getSortOrder() == null ? 999 : s.getSortOrder()))
                .map(this::mapStationToNode)
                .collect(Collectors.toList())
                : new ArrayList<>();

        return PositionHierarchyDto.CategoryNodeDto.builder()
                .id(cat.getId())
                .name(cat.getName())
                .color(cat.getHexColor())
                .isActive(cat.getIsActive())
                .sortOrder(cat.getSortOrder())
                .stations(stationNodes)
                .build();
    }

    private PositionHierarchyDto.StationNodeDto mapStationToNode(Station stat) {
        List<PositionHierarchyDto.TemplateNodeDto> templateNodes = (stat.getTemplates() != null)
                ? stat.getTemplates().stream()
                .sorted(Comparator.comparing(t -> t.getSortOrder() == null ? 999 : t.getSortOrder()))
                .map(this::mapTemplateToNode)
                .collect(Collectors.toList())
                : new ArrayList<>();

        return PositionHierarchyDto.StationNodeDto.builder()
                .id(stat.getId())
                .name(stat.getName())
                .isActive(stat.getIsActive())
                .capacityLimit(stat.getCapacityLimit())
                .needsQualification(stat.getNeedsQualification())
                .sortOrder(stat.getSortOrder())
                .templates(templateNodes)
                .build();
    }

    private PositionHierarchyDto.TemplateNodeDto mapTemplateToNode(ShiftTemplate tmpl) {
        return PositionHierarchyDto.TemplateNodeDto.builder()
                .id(tmpl.getId())
                .name(tmpl.getName())
                .workersNeeded(tmpl.getWorkersNeeded())
                .startTime(tmpl.getStartTime() != null ? tmpl.getStartTime().toString() : null)
                .endTime(tmpl.getEndTime() != null ? tmpl.getEndTime().toString() : null)
                .startTime2(tmpl.getStartTime2() != null ? tmpl.getStartTime2().toString() : null)
                .endTime2(tmpl.getEndTime2() != null ? tmpl.getEndTime2().toString() : null)
                .isActive(tmpl.getIsActive())
                .sortOrder(tmpl.getSortOrder())
                .useOpeningHours(tmpl.getUseOpeningHours())
                .hasDopo(tmpl.getHasDopo())
                .hasOdpo(tmpl.getHasOdpo())
                .build();
    }
}