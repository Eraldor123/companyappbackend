package com.companyapp.backend.dev;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dev/seed")
@RequiredArgsConstructor
public class DataSeederController {

    private final DataSeederService dataSeederService;

    // Přístup má pouze administrátor (chráníme se, aby nám armádu negeneroval běžný brigádník)
    @PostMapping("/army")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> seedArmy(@RequestParam(defaultValue = "30") int count) {
        String result = dataSeederService.seedArmy(count);
        return ResponseEntity.ok(result);
    }
}
