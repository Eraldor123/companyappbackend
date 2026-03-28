package com.companyapp.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String action;      // např. "DELETE_SHIFT", "ASSIGN_USER"
    private String entityName;  // např. "Shift"
    private String entityId;    // ID směny (uloženo jako String pro flexibilitu)
    private String performedBy; // E-mail nebo jméno manažera, který to udělal
    private LocalDateTime timestamp;

    @Column(length = 1000)
    private String details;     // např. "Zrušena směna od 8:00 do 16:00"
}