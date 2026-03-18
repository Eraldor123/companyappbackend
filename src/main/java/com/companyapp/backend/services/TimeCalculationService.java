package com.companyapp.backend.services;

import java.time.Duration;
import java.time.ZonedDateTime;

public interface TimeCalculationService {
    Duration calculateNetWorkTime(ZonedDateTime startTime, ZonedDateTime endTime);
}