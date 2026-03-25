package com.companyapp.backend.services.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class PositionHierarchyDto {
    private List<CategoryNodeDto> categories;

    @Data
    @Builder
    public static class CategoryNodeDto {
        private Integer id;
        private String name;
        private String color;
        private Boolean isActive;
        private List<StationNodeDto> stations;
    }

    @Data
    @Builder
    public static class StationNodeDto {
        private Integer id;
        private String name;
        private Boolean isActive;
        private List<TemplateNodeDto> templates;
    }

    @Data
    @Builder
    public static class TemplateNodeDto {
        private Integer id;
        private String name;
        private String timeRange;
        private String startTime;
        private String endTime;
        private String startTime2;
        private String endTime2;
        private Integer workersNeeded;
        private Boolean isActive;
    }
}
