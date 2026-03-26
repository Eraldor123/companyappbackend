// src/main/java/com/companyapp/backend/services/ShiftGenerationService.java
package com.companyapp.backend.services;

import java.time.LocalDate;
import java.util.List;

public interface ShiftGenerationService {
    // ZMĚNĚNO z UUID na Integer u templateId
    List<Object> generateShiftsFromTemplate(LocalDate startDate, LocalDate endDate, Integer templateId);
    void copyWeekSchedule(LocalDate sourceWeekStart, LocalDate targetWeekStart);
    void clearWeekSchedule(LocalDate startDate, LocalDate endDate);
}