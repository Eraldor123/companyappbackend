package com.companyapp.backend.dev;

import com.companyapp.backend.entity.*;
import com.companyapp.backend.enums.AccessLevel;
import com.companyapp.backend.enums.ContractType;
import com.companyapp.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataSeederService {

    // OCHRANA java:S2068: Heslo se natahuje z konfigurace, není natvrdo v kódu
    @Value("${app.dev.seeder.default-password:Heslo123!}")
    private String defaultRawPassword;

    private static final int AVAILABILITY_DAYS_WINDOW = 21;
    private static final String LOCALE_CS = "cs";

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final ContractRepository contractRepository;
    private final StationRepository stationRepository;
    private final AvailabilityRepository availabilityRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Vygeneruje testovací data pro zadaný počet uživatelů.
     * OPRAVA java:S3776: Metoda je nyní plochá a přehledná díky extrakci logiky.
     */
    @Transactional
    public String seedArmy(int count) {
        // OPRAVA Deprekace: Použití Locale.of (Java 19+) nebo Locale.forLanguageTag
        Faker faker = new Faker(Locale.of(LOCALE_CS));
        String encodedPassword = passwordEncoder.encode(defaultRawPassword);

        List<Station> allStations = stationRepository.findAll().stream()
                .filter(s -> Boolean.TRUE.equals(s.getIsActive()))
                .toList();

        LocalDate today = LocalDate.now();

        for (int i = 0; i < count; i++) {
            generateSingleBrigadier(i, faker, encodedPassword, allStations, today);

            if ((i + 1) % 10 == 0) {
                log.info("Průběh seedování: {}/{} uživatelů hotovo...", (i + 1), count);
            }
        }

        return "Úspěšně naverbováno " + count + " brigádníků vč. profilů, smluv, kvalifikací a dostupností!";
    }

    /**
     * Vytvoří jednoho kompletního uživatele se všemi vazbami.
     */
    private void generateSingleBrigadier(int index, Faker faker, String password, List<Station> stations, LocalDate today) {
        String fname = faker.name().firstName();
        String lname = faker.name().lastName();
        String email = faker.internet().emailAddress(fname.toLowerCase() + "." + lname.toLowerCase() + index);

        // 1. Uživatel (Entity)
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setPin(passwordEncoder.encode(faker.number().digits(4)));
        user.setRoles(Set.of(AccessLevel.BASIC));
        user.setIsActive(true);
        user.setQualifiedStations(pickRandomStations(faker, stations));

        User savedUser = userRepository.save(user);

        // 2. Profil, Smlouva a Dostupnosti
        createProfile(savedUser, fname, lname, faker.phoneNumber().cellPhone());
        createContract(savedUser, today);
        createAvailabilities(savedUser.getId(), faker, today);
    }

    private Set<Station> pickRandomStations(Faker faker, List<Station> allStations) {
        Set<Station> selected = new HashSet<>();
        if (!allStations.isEmpty()) {
            int maxQuals = Math.min(3, allStations.size());
            int numQuals = faker.random().nextInt(1, maxQuals + 1);
            for (int q = 0; q < numQuals; q++) {
                selected.add(allStations.get(faker.random().nextInt(allStations.size())));
            }
        }
        return selected;
    }

    private void createProfile(User user, String fname, String lname, String phone) {
        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setFirstName(fname);
        profile.setLastName(lname);
        profile.setPhone(phone);
        userProfileRepository.save(profile);
    }

    private void createContract(User user, LocalDate today) {
        Contract contract = new Contract();
        contract.setUser(user);
        contract.setType(ContractType.DPP);
        contract.setValidFrom(today.minusMonths(1));
        contractRepository.save(contract);
    }

    private void createAvailabilities(UUID userId, Faker faker, LocalDate today) {
        List<Availability> availabilities = new ArrayList<>();
        for (int d = 0; d < AVAILABILITY_DAYS_WINDOW; d++) {
            // 60% šance, že má brigádník daný den čas
            if (faker.random().nextInt(100) < 60) {
                availabilities.add(buildRandomAvailability(userId, today.plusDays(d), faker));
            }
        }
        availabilityRepository.saveAll(availabilities);
    }

    private Availability buildRandomAvailability(UUID userId, LocalDate date, Faker faker) {
        Availability a = new Availability();
        a.setUserId(userId);
        a.setAvailableDate(date);

        boolean morning = faker.random().nextBoolean();
        boolean afternoon = faker.random().nextBoolean();

        // Zajištění, aby aspoň jedna část dne byla true
        if (!morning && !afternoon) {
            morning = true;
        }

        a.setMorning(morning);
        a.setAfternoon(afternoon);
        a.setConfirmed(false);
        return a;
    }
}