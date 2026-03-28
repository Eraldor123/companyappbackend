package com.companyapp.backend.services.impl;

import com.companyapp.backend.entity.MainCategory;
import com.companyapp.backend.entity.Station;
import com.companyapp.backend.entity.ShiftTemplate;
import com.companyapp.backend.repository.MainCategoryRepository;
import com.companyapp.backend.repository.ShiftTemplateRepository;
import com.companyapp.backend.repository.StationRepository;
import com.companyapp.backend.services.PositionSettingsService;
import com.companyapp.backend.services.dto.response.PositionHierarchyDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PositionSettingsServiceImpl implements PositionSettingsService {

    private final MainCategoryRepository categoryRepository;
    private final StationRepository stationRepository;
    private final ShiftTemplateRepository templateRepository;

    @Override
    @Transactional(readOnly = true)
    public PositionHierarchyDto getFullHierarchy() {
        List<MainCategory> allCategories = categoryRepository.findAll();
        List<Station> allStations = stationRepository.findAll();

        List<PositionHierarchyDto.CategoryNodeDto> categoryNodes = allCategories.stream()
                .sorted(Comparator.comparing(c -> c.getSortOrder() == null ? 999 : c.getSortOrder()))
                .map(cat -> {

                    List<PositionHierarchyDto.StationNodeDto> stationNodes = allStations.stream()
                            .filter(s -> s.getCategory().getId().equals(cat.getId()))
                            .sorted(Comparator.comparing(s -> s.getSortOrder() == null ? 999 : s.getSortOrder()))
                            .map(stat -> {

                                List<ShiftTemplate> templates = templateRepository.findByStationId(stat.getId());

                                List<PositionHierarchyDto.TemplateNodeDto> templateNodes = templates.stream()
                                        .sorted(Comparator.comparing(t -> t.getSortOrder() == null ? 999 : t.getSortOrder()))
                                        .map(tmpl -> {

                                            String timeRange;
                                            if (Boolean.TRUE.equals(tmpl.getUseOpeningHours())) {
                                                timeRange = "Dle otevírací doby";
                                            } else {
                                                String s1 = tmpl.getStartTime() != null ? tmpl.getStartTime().toString() : "?";
                                                String e1 = tmpl.getEndTime() != null ? tmpl.getEndTime().toString() : "?";
                                                timeRange = s1 + " - " + e1;

                                                if (tmpl.getStartTime2() != null && tmpl.getEndTime2() != null) {
                                                    timeRange += " a " + tmpl.getStartTime2() + " - " + tmpl.getEndTime2();
                                                }
                                            }

                                            return PositionHierarchyDto.TemplateNodeDto.builder()
                                                    .id(tmpl.getId())
                                                    .name(tmpl.getName())
                                                    .timeRange(timeRange)
                                                    .startTime(tmpl.getStartTime() != null ? tmpl.getStartTime().toString() : null)
                                                    .endTime(tmpl.getEndTime() != null ? tmpl.getEndTime().toString() : null)
                                                    .startTime2(tmpl.getStartTime2() != null ? tmpl.getStartTime2().toString() : null)
                                                    .endTime2(tmpl.getEndTime2() != null ? tmpl.getEndTime2().toString() : null)
                                                    .workersNeeded(tmpl.getWorkersNeeded())
                                                    .isActive(tmpl.getIsActive())
                                                    .sortOrder(tmpl.getSortOrder())
                                                    .useOpeningHours(tmpl.getUseOpeningHours())
                                                    .hasDopo(tmpl.getHasDopo())
                                                    .hasOdpo(tmpl.getHasOdpo())
                                                    .build();
                                        }).collect(Collectors.toList());

                                return PositionHierarchyDto.StationNodeDto.builder()
                                        .id(stat.getId())
                                        .name(stat.getName())
                                        .capacityLimit(stat.getCapacityLimit())
                                        .needsQualification(stat.getNeedsQualification())
                                        .isActive(stat.getIsActive())
                                        .sortOrder(stat.getSortOrder())
                                        .templates(templateNodes)
                                        .build();
                            }).collect(Collectors.toList());

                    return PositionHierarchyDto.CategoryNodeDto.builder()
                            .id(cat.getId())
                            .name(cat.getName())
                            .color(cat.getHexColor())
                            .sortOrder(cat.getSortOrder())
                            .isActive(cat.getIsActive())
                            .stations(stationNodes)
                            .build();
                }).collect(Collectors.toList());

        return PositionHierarchyDto.builder().categories(categoryNodes).build();
    }
}