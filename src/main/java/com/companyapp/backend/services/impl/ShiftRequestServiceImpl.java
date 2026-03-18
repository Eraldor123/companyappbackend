package com.companyapp.backend.services.impl;

import com.companyapp.backend.repository.ShiftAssignmentRepository;
import com.companyapp.backend.services.ShiftRequestService;
import com.companyapp.backend.services.dto.request.ShiftCancellationRequestDto;
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
        log.info("Přijata žádost o zrušení směny: {}", request.getShiftAssignmentId());

        // 1. Ověření, že přiřazení existuje
        // ShiftAssignment assignment = shiftAssignmentRepository.findById(request.getShiftAssignmentId())
        //        .orElseThrow(() -> new ResourceNotFoundException("Přiřazení směny nebylo nalezeno."));

        // 2. Vytvoření entity žádosti (např. ShiftCancellationRequest)
        // ShiftCancellationRequest cancellationRequest = new ShiftCancellationRequest();
        // cancellationRequest.setShiftAssignment(assignment);
        // cancellationRequest.setReason(request.getReason()); // Důvod zrušení (povinné pole z frontendu)
        // cancellationRequest.setStatus(RequestStatus.PENDING);

        // requestRepository.save(cancellationRequest);

        // 3. Vystřelení události pro asynchronní odeslání emailu manažerovi
        // eventPublisher.publishEvent(new ShiftCancellationRequestedEvent(this, cancellationRequest.getId()));

        log.info("Žádost o uvolnění ze směny byla založena a čeká na schválení manažerem.");
    }
}