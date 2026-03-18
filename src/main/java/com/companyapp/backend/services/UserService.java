package com.companyapp.backend.services;

import com.companyapp.backend.services.dto.request.UserRegistrationDto;
import com.companyapp.backend.services.dto.response.UserProfileDto;

import java.util.UUID;

public interface UserService {
    UserProfileDto registerUser(UserRegistrationDto request);
    void deactivateUser(UUID userId);
    void hardDeleteUser(UUID userId);
    UserProfileDto getUserProfile(UUID userId);
}