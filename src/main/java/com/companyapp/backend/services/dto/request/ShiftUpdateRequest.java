package com.companyapp.backend.services.dto.request;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ShiftUpdateRequest {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int requiredCapacity;
    private String description; // <--- PŘIDÁNO
}