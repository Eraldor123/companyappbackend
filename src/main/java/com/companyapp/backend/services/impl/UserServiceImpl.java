package com.companyapp.backend.services.impl;

import com.companyapp.backend.entity.Contract;
import com.companyapp.backend.entity.User;
import com.companyapp.backend.entity.UserProfile;
import com.companyapp.backend.enums.AccessLevel;
import com.companyapp.backend.enums.ContractType;
import com.companyapp.backend.repository.ContractRepository;
import com.companyapp.backend.repository.UserRepository;
import com.companyapp.backend.repository.UserProfileRepository;
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
    private final ContractRepository contractRepository; // PŘIDÁNO: Repozitář pro smlouvy
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserProfileDto registerUser(UserRegistrationDto request) {
        log.info("Zahajuji registraci uživatele s e-mailem: {}", request.getEmail());

        // 1. Kontrola unikátnosti
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Uživatel s e-mailem " + request.getEmail() + " již existuje.");
        }

        // 2. Vytvoření doménové entity User
        User user = new User();
        user.setEmail(request.getEmail());
        user.setRoles(request.getAccessLevels());
        user.setActive(true);

        String generatedPin;
        String hashedPin;
        do {
            generatedPin = user.getRoles().contains(AccessLevel.TERMINAL) ? "0000" : String.format("%04d", new java.util.Random().nextInt(10000));
            hashedPin = passwordEncoder.encode(generatedPin);
        } while (userRepository.findByPinAndIsActiveTrue(hashedPin).isPresent());

        log.info("Vygenerován UNIKÁTNÍ PIN pro uživatele (odeslat na email): {}", generatedPin);
        user.setPin(hashedPin);

        // 3. Vytvoření přidruženého profilu
        UserProfile profile = new UserProfile();
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setPhone(request.getPhone());
        profile.setUser(user);

        // 4. Vytvoření entity Contract (Smlouva)
        Contract contract = new Contract();
        contract.setUser(user);
        contract.setType(request.getContractType());
        contract.setHourlyWage(request.getHourlyWage());
        contract.setMonthlyWage(request.getMonthlyWage());

        // Převod Double (FTE z DTO) na BigDecimal (FTE v entitě)
        if (request.getContractSize() != null) {
            contract.setFte(BigDecimal.valueOf(request.getContractSize()));
        }
        if (contract.getType() == ContractType.OSVC)
            contract.setCompanyIdNumber(request.getIco());
        contract.setValidFrom(LocalDate.now()); // Smlouva platí od okamžiku registrace

        // 5. Uložení všech entit do databáze
        userRepository.save(user);
        userProfileRepository.save(profile);
        contractRepository.save(contract); // Uložíme smlouvu

        return mapToProfileDto(user, profile, contract);
    }

    @Override
    @Transactional
    public void deactivateUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Uživatel nenalezen."));
        user.setActive(false);
        userRepository.save(user);
        log.info("Uživatel {} byl deaktivován. Historie zůstala zachována.", userId);
    }

    @Override
    @Transactional
    public void hardDeleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Uživatel nenalezen."));

        userRepository.delete(user);
        log.warn("Uživatel {} byl TVRDĚ smazán vč. přerušení relací.", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileDto getUserProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Uživatel nenalezen."));

        if (user.getUserProfile() == null) {
            throw new ResourceNotFoundException("Profil uživatele nenalezen.");
        }

        // Najdeme aktivní smlouvu uživatele, abychom ji mohli zobrazit v profilu
        Contract contract = contractRepository.findLatestContractByUserId(user.getId()).orElse(null);

        return mapToProfileDto(user, user.getUserProfile(), contract);
    }

    // PŘIDÁNO: Parametr Contract, aby se do DTO propisoval i typ smlouvy
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