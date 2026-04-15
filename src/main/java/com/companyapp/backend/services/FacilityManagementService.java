package com.companyapp.backend.services;

public interface FacilityManagementService {

    void deactivateStation(Integer stationId);

    void deactivateMainCategory(Integer categoryId);

    /**
     * Tato metoda slouží k nevratnému promazání stanovišť (např. při chybném importu dat).
     * Označeno SuppressWarnings, protože v aktuální verzi frontendu není volána.
     */
    @SuppressWarnings("unused")
    void hardDeleteStation(Integer stationId, String confirmationText);
}