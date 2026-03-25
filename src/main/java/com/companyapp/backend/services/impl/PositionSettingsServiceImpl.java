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
        // ZMĚNA: Načteme VŠECHNY kategorie (aby frontend mohl zobrazit i ty neaktivní přes očičko)
        List<MainCategory> categories = categoryRepository.findAll();

        List<PositionHierarchyDto.CategoryNodeDto> categoryNodes = categories.stream().map(cat -> {

            List<Station> stations = stationRepository.findAll().stream()
                    .filter(s -> s.getCategory().getId().equals(cat.getId()))
                    .collect(Collectors.toList());

            List<PositionHierarchyDto.StationNodeDto> stationNodes = stations.stream().map(stat -> {

                List<ShiftTemplate> templates = templateRepository.findByStationId(stat.getId());

                List<PositionHierarchyDto.TemplateNodeDto> templateNodes = templates.stream().map(tmpl -> {
                    String timeRange = tmpl.getStartTime() + " - " + tmpl.getEndTime();
                    if (tmpl.getStartTime2() != null && tmpl.getEndTime2() != null) {
                        timeRange += " a " + tmpl.getStartTime2() + " - " + tmpl.getEndTime2();
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
                            .isActive(tmpl.getIsActive()) // PŘIDÁNO
                            .build();
                }).collect(Collectors.toList());

                return PositionHierarchyDto.StationNodeDto.builder()
                        .id(stat.getId())
                        .name(stat.getName())
                        .isActive(stat.getIsActive()) // PŘIDÁNO (Tohle opraví ten bug ve frontendu!)
                        .templates(templateNodes)
                        .build();
            }).collect(Collectors.toList());

            return PositionHierarchyDto.CategoryNodeDto.builder()
                    .id(cat.getId())
                    .name(cat.getName())
                    .color(cat.getHexColor())
                    .isActive(cat.getIsActive()) // PŘIDÁNO
                    .stations(stationNodes)
                    .build();
        }).collect(Collectors.toList());

        return PositionHierarchyDto.builder().categories(categoryNodes).build();
    }
}