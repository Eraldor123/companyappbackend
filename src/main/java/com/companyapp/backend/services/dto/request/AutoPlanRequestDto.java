package com.companyapp.backend.services.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data // Vygeneruje gettery, settery, toString atd.
public class AutoPlanRequestDto {
    private int fairnessWeight;  // 0-100 (férovost k dříčům)
    private int trainingWeight;  // 0-100 (míra zaučování)
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate targetDate;
    private Integer categoryId;  // <--- PŘIDÁNO: Filtrování podle aktuální kategorie v UI
}