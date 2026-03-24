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
        // 1. Načteme všechny aktivní kategorie [cite: 56]
        List<MainCategory> categories = categoryRepository.findByIsActiveTrue();

        List<PositionHierarchyDto.CategoryNodeDto> categoryNodes = categories.stream().map(cat -> {
            // 2. Pro každou kategorii najdeme její stanoviště [cite: 51]
            // OPRAVA: Použit cat.getId() místo cat.id
            List<Station> stations = stationRepository.findAll().stream()
                    .filter(s -> s.getCategory().getId().equals(cat.getId()))
                    .collect(Collectors.toList());

            List<PositionHierarchyDto.StationNodeDto> stationNodes = stations.stream().map(stat -> {
                // 3. Pro každé stanoviště najdeme jeho šablony [cite: 58]
                List<ShiftTemplate> templates = templateRepository.findByStationId(stat.getId());

                List<PositionHierarchyDto.TemplateNodeDto> templateNodes = templates.stream().map(tmpl ->
                        PositionHierarchyDto.TemplateNodeDto.builder()
                                .id(tmpl.getId())
                                .name(tmpl.getName())
                                .timeRange(tmpl.getStartTime() + " - " + tmpl.getEndTime())
                                .workersNeeded(tmpl.getWorkersNeeded())
                                .build()
                ).collect(Collectors.toList());

                return PositionHierarchyDto.StationNodeDto.builder()
                        .id(stat.getId())
                        .name(stat.getName())
                        .templates(templateNodes)
                        .build();
            }).collect(Collectors.toList());

            return PositionHierarchyDto.CategoryNodeDto.builder()
                    .id(cat.getId())
                    .name(cat.getName())
                    .color(cat.getHexColor())
                    .stations(stationNodes)
                    .build();
        }).collect(Collectors.toList());

        // OPRAVA: Použito .builder() místo .PositionHierarchyDtoBuilder()
        return PositionHierarchyDto.builder().categories(categoryNodes).build();
    }
}