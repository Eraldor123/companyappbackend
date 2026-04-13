package com.companyapp.backend.services;

import com.companyapp.backend.services.dto.request.AutoPlanRequestDto;

public interface AutoPlanService {
    /**
     * Spustí algoritmus pro automatické obsazení volných směn.
     * @param request Obsahuje váhy pro férovost, zaučování a cílové datum.
     */
    void runAutoPlanning(AutoPlanRequestDto request);
}