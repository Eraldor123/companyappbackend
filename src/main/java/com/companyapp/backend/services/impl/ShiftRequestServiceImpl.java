package com.companyapp.backend.services.impl;

import com.companyapp.backend.entity.ShiftAssignment;
import com.companyapp.backend.repository.ShiftAssignmentRepository;
import com.companyapp.backend.services.ShiftRequestService;
import com.companyapp.backend.services.dto.request.ShiftCancellationRequestDto;
import com.companyapp.backend.services.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShiftRequestServiceImpl implements ShiftRequestService {

    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void submitCancellationRequest(ShiftCancellationRequestDto request) {
        log.info("Přijata žádost o zrušení přiřazení: {}", request.getShiftAssignmentId());

        // 1. Ověření existence přiřazení přes repozitář
        ShiftAssignment assignment = shiftAssignmentRepository.findById(request.getShiftAssignmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Přiřazení směny nebylo nalezeno."));

        // 2. Logování důvodu (v budoucnu uložení do tabulky žádostí)
        log.info("Uživatel {} žádá o zrušení směny z důvodu: {}",
                assignment.getEmployee().getEmail(), request.getReason());

        // 3. Vystřelení události pro budoucí notifikace (např. e-mail manažerovi)
        // Zde využíváme tvůj eventPublisher, který máš už v konstruktoru
        eventPublisher.publishEvent(request);

        log.info("Žádost o uvolnění ze směny byla úspěšně zpracována systémem.");
    }
}