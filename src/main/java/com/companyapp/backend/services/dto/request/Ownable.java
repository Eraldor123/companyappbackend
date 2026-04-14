package com.companyapp.backend.services.dto.request;

import java.util.UUID;

/**
 * Rozhraní pro DTO objekty, které nesou identifikátor vlastníka.
 * Slouží k extrémně rychlé kontrole IDOR bez nutnosti použít reflexi.
 */
public interface Ownable {
    UUID getOwnerId();
}