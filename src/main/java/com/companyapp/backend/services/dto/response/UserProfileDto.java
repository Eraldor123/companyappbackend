package com.companyapp.backend.services.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserProfileDto {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String attendanceId;
    private String contractType;
    private String accessLevel;
    private boolean isActive;
}
