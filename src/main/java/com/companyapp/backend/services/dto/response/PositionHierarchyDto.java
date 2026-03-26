package com.companyapp.backend.services.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty; // TENTO IMPORT JE DŮLEŽITÝ
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

        @JsonProperty("isActive") // OPRAVA: Vynutí přesný název v JSONu
        private Boolean isActive;

        private List<StationNodeDto> stations;
        private Integer sortOrder;
    }

    @Data
    @Builder
    public static class StationNodeDto {
        private Integer id;
        private String name;

        @JsonProperty("isActive") // OPRAVA
        private Boolean isActive;

        private List<TemplateNodeDto> templates;
        private Integer capacityLimit;
        private Boolean needsQualification;
        private Integer sortOrder; // Přidáno pro řazení
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

        @JsonProperty("isActive") // OPRAVA
        private Boolean isActive;

        private Integer sortOrder; // Přidáno pro řazení
        private Boolean useOpeningHours;
        private Boolean hasDopo;
        private Boolean hasOdpo;
    }
}