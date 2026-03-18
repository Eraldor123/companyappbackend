package com.companyapp.backend.services;

import java.time.LocalDate;

public interface OperatingHoursService {
    // Vrací např. objekt obsahující časy od-do a informaci, zda jde o Sezónní režim (Halloween atd.)
    Object getOperatingHoursForDate(LocalDate date);
}