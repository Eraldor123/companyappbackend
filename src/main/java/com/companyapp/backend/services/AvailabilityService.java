package com.companyapp.backend.services;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

public interface AvailabilityService {
    void submitMonthlyAvailability(UUID userId, YearMonth month, List<LocalDate> availableDays);
}