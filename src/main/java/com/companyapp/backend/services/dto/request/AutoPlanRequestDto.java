package com.companyapp.backend.services.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import java.time.LocalDate;

@Data
public class AutoPlanRequestDto {

    @Min(value = 0, message = "Váha férovosti musí být minimálně 0.")
    @Max(value = 100, message = "Váha férovosti nesmí přesáhnout 100.")
    private int fairnessWeight;

    @Min(value = 0, message = "Váha zaučování musí být minimálně 0.")
    @Max(value = 100, message = "Váha zaučování nesmí přesáhnout 100.")
    private int trainingWeight;

    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate targetDate;
    private Integer categoryId;
}