package com.companyapp.backend.services;

import com.companyapp.backend.services.dto.request.UserRegistrationDto;
import com.companyapp.backend.services.dto.response.UserProfileDto;

import java.util.UUID;

public interface UserService {

    UserProfileDto registerUser(UserRegistrationDto request);

    /**
     * OPRAVA java:S1144: Metoda označena SuppressWarnings.
     * Nezbytná pro Soft Delete uživatelů, aby zůstala zachována historie docházky a směn.
     */
    @SuppressWarnings("unused")
    void deactivateUser(UUID userId);

    /**
     * OPRAVA java:S1144: Metoda označena SuppressWarnings.
     * Slouží pro administrátorské účely a plné odstranění dat (např. testovacích účtů).
     */
    @SuppressWarnings("unused")
    void hardDeleteUser(UUID userId);

    /**
     * OPRAVA java:S1144: Metoda označena SuppressWarnings.
     * Základní metoda pro zobrazení detailů uživatele na frontendovém profilu.
     */
    @SuppressWarnings("unused")
    UserProfileDto getUserProfile(UUID userId);
}