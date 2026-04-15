package com.companyapp.backend.services.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class PositionHierarchyDto {

    @Builder.Default // OCHRANA PROTI NULL
    private List<CategoryNodeDto> categories = new ArrayList<>();

    @Data
    @Builder
    public static class CategoryNodeDto {
        private Integer id;
        private String name;
        private String color;

        @JsonProperty("isActive")
        private Boolean isActive;

        @Builder.Default // OCHRANA PROTI NULL
        private List<StationNodeDto> stations = new ArrayList<>();
        private Integer sortOrder;
    }

    @Data
    @Builder
    public static class StationNodeDto {
        private Integer id;
        private String name;

        @JsonProperty("isActive")
        private Boolean isActive;

        @Builder.Default // OCHRANA PROTI NULL
        private List<TemplateNodeDto> templates = new ArrayList<>();
        private Integer capacityLimit;
        private Boolean needsQualification;
        private Integer sortOrder;
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

        @JsonProperty("isActive")
        private Boolean isActive;

        private Integer sortOrder;
        private Boolean useOpeningHours;
        private Boolean hasDopo;
        private Boolean hasOdpo;
    }
}