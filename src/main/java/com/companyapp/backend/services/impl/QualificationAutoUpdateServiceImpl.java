package com.companyapp.backend.services.impl;

import com.companyapp.backend.entity.ShiftAssignment;
import com.companyapp.backend.entity.Station;
import com.companyapp.backend.entity.User;
import com.companyapp.backend.repository.ShiftAssignmentRepository;
import com.companyapp.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QualificationAutoUpdateServiceImpl {

    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final UserRepository userRepository;

    /**
     * Spustí se automaticky každý den ve 3:00 ráno (serverového času).
     * Zkontroluje včerejší směny a přidělí kvalifikace nováčkům, kteří je úspěšně odpracovali.
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void processCompletedTrainings() {

        // Zajímají nás směny z předchozího dne (toho, který právě skončil)
        LocalDate targetDate = LocalDate.now().minusDays(1);

        log.info("Spouštím noční kontrolu a zápis kvalifikací za den: {}", targetDate);

        List<ShiftAssignment> assignments = shiftAssignmentRepository.findByShiftDateBetween(targetDate, targetDate);

        int newlyQualifiedCount = 0;

        for (ShiftAssignment assignment : assignments) {
            User employee = assignment.getEmployee();
            Station station = assignment.getShift().getStation();

            // Pokud stanoviště vyžaduje kvalifikaci a zaměstnanec ji ještě NEMÁ
            if (Boolean.TRUE.equals(station.getNeedsQualification()) && !employee.getQualifiedStations().contains(station)) {

                employee.getQualifiedStations().add(station);
                userRepository.save(employee);
                newlyQualifiedCount++;

                log.info("🎓 Zaměstnanec {} automaticky získal kvalifikaci na stanoviště '{}'.",
                        employee.getLastName(), station.getName());
            }
        }

        log.info("Noční zpracování dokončeno. Úspěšně zaučeno a kvalifikováno {} zaměstnanců.", newlyQualifiedCount);
    }
}