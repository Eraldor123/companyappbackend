package com.companyapp.backend.services;

import java.util.UUID;

public interface TerminalAuthenticationService {
    // Vrací UUID uživatele, pokud je kombinace Docházkové ID a PIN správná
    UUID authenticateTerminal(String attendanceId, String rawPin);
}