package com.companyapp.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @GetMapping("/verify")
    public ResponseEntity<Void> verifyToken() {
        // Pokud požadavek projde až sem, znamená to, že Spring Security (JwtAuthenticationFilter)
        // token úspěšně ověřil a je platný. Můžeme v klidu vrátit 200 OK.
        return ResponseEntity.ok().build();
    }
}