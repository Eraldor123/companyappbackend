package com.companyapp.backend.services.impl;

import com.companyapp.backend.entity.Contract;
import com.companyapp.backend.entity.User;
import com.companyapp.backend.entity.UserProfile;
import com.companyapp.backend.enums.AccessLevel;
import com.companyapp.backend.enums.ContractType;
import com.companyapp.backend.repository.ContractRepository;
import com.companyapp.backend.repository.UserProfileRepository;
import com.companyapp.backend.repository.UserRepository;
import com.companyapp.backend.services.AuditLogService;
import com.companyapp.backend.services.EmailService;
import com.companyapp.backend.services.UserService;
import com.companyapp.backend.services.dto.request.UserRegistrationDto;
import com.companyapp.backend.services.dto.response.UserProfileDto;
import com.companyapp.backend.services.exception.DuplicateResourceException;
import com.companyapp.backend.services.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final ContractRepository contractRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService; // PŘIDÁNO: Záznam do auditu
    private final EmailService emailService; // PŘIDÁNO: Pro odesílání e-mailů

    private static final String userNotFound = "Uživatel nenalezen.";

    @Override
    @Transactional
    public UserProfileDto registerUser(UserRegistrationDto request) {
        log.info("Zahajuji registraci uživatele s e-mailem: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Uživatel s e-mailem " + request.getEmail() + " již existuje.");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setRoles(request.getAccessLevels());
        user.setActive(true);

        // 1. Generování PINu pro terminál (zůstává)
        String generatedPin;
        String hashedPin;
        do {
            generatedPin = user.getRoles().contains(AccessLevel.TERMINAL) ? "0000" : String.format("%04d", new java.util.Random().nextInt(10000));
            hashedPin = passwordEncoder.encode(generatedPin);
        } while (userRepository.findByPinAndIsActiveTrue(hashedPin).isPresent());
        user.setPin(hashedPin);

        // 2. PŘIDÁNO: Generování hesla pro web (např. 8 náhodných znaků)
        String generatedPassword = UUID.randomUUID().toString().substring(0, 8);
        user.setPassword(passwordEncoder.encode(generatedPassword));

        log.warn("==========================================================");
        log.warn("NOVÝ UŽIVATEL: {}", request.getEmail());
        log.warn("JEHO PIN (TERMINÁL): {}", generatedPin);
        log.warn("JEHO HESLO (WEB): {}", generatedPassword);
        log.warn("==========================================================");

        UserProfile profile = new UserProfile();
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setPhone(request.getPhone());
        profile.setUser(user);

        Contract contract = new Contract();
        contract.setUser(user);
        contract.setType(request.getContractType());
        contract.setHourlyWage(request.getHourlyWage());
        contract.setMonthlyWage(request.getMonthlyWage());

        if (request.getContractSize() != null) {
            contract.setFte(BigDecimal.valueOf(request.getContractSize()));
        }
        if (contract.getType() == ContractType.OSVC)
            contract.setCompanyIdNumber(request.getIco());
        contract.setValidFrom(LocalDate.now());

        userRepository.save(user);
        userProfileRepository.save(profile);
        contractRepository.save(contract);

        // ZÁZNAM DO AUDITU
        auditLogService.logAction(
                "CREATE_USER",
                "User",
                user.getId().toString(),
                "Vytvořen nový uživatel: " + user.getEmail() + " (Smlouva: " + contract.getType() + ")."
        );

        if (request.isSendPassword()) {
            // Tady bys mohl vytvořit metodu v EmailService pro uvítací e-mail
            emailService.sendRegistrationEmail(user.getEmail(), generatedPin, generatedPassword);
            log.info("E-mail s přístupovými údaji odeslán na: {}", user.getEmail());
        }

        return mapToProfileDto(user, profile, contract);
    }

    @Override
    @Transactional
    public void deactivateUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(userNotFound));
        user.setActive(false);
        userRepository.save(user);
        log.info("Uživatel {} byl deaktivován. Historie zůstala zachována.", userId);

        // ZÁZNAM DO AUDITU
        auditLogService.logAction(
                "DEACTIVATE_USER",
                "User",
                userId.toString(),
                "Uživatel " + user.getEmail() + " byl deaktivován (Soft delete)."
        );
    }

    @Override
    @Transactional
    public void hardDeleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(userNotFound));

        String deletedEmail = user.getEmail();
        userRepository.delete(user);
        log.warn("Uživatel {} byl TVRDĚ smazán vč. přerušení relací.", userId);

        // ZÁZNAM DO AUDITU
        auditLogService.logAction(
                "HARD_DELETE_USER",
                "User",
                userId.toString(),
                "Uživatel " + deletedEmail + " byl NENÁVRATNĚ SMAZÁN z databáze."
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileDto getUserProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(userNotFound));

        if (user.getUserProfile() == null) {
            throw new ResourceNotFoundException("Profil uživatele nenalezen.");
        }

        Contract contract = contractRepository.findLatestContractByUserId(user.getId()).orElse(null);

        return mapToProfileDto(user, user.getUserProfile(), contract);
    }

    private UserProfileDto mapToProfileDto(User user, UserProfile profile, Contract contract) {
        return UserProfileDto.builder()
                .id(user.getId())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .email(user.getEmail())
                .isActive(user.isActive())
                .contractType(contract != null && contract.getType() != null ? contract.getType().name() : "N/A")
                .build();
    }
}