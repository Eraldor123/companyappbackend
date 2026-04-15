package com.companyapp.backend.dev;

import com.companyapp.backend.entity.*;
import com.companyapp.backend.enums.AccessLevel;
import com.companyapp.backend.enums.ContractType;
import com.companyapp.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataSeederService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final ContractRepository contractRepository;
    private final StationRepository stationRepository;
    private final AvailabilityRepository availabilityRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public String seedArmy(int count) {
        Faker faker = new Faker(new Locale("cs")); // Česká jména a telefony
        String defaultPassword = passwordEncoder.encode("Heslo123!"); // Všichni budou mít stejné heslo

        // Získáme všechna aktivní stanoviště
        List<Station> allStations = stationRepository.findAll().stream()
                .filter(s -> Boolean.TRUE.equals(s.getIsActive()))
                .toList();

        LocalDate today = LocalDate.now();

        int generatedCount = 0;

        for (int i = 0; i < count; i++) {
            // 1. Vytvoření Entity User
            User user = new User();
            String fname = faker.name().firstName();
            String lname = faker.name().lastName();
            String email = faker.internet().emailAddress(fname.toLowerCase() + "." + lname.toLowerCase() + i);

            user.setEmail(email);
            user.setPassword(defaultPassword);
            user.setPin(passwordEncoder.encode(faker.number().digits(4)));

            // Použití Lombok vygenerovaných setterů
            user.setRoles(Set.of(AccessLevel.BASIC)); // Obyčejný brigádník
            user.setIsActive(true); // Změněno z setActive(true), protože Lombok generuje setIsActive() pro Boolean pole

            // 2. Náhodné Kvalifikace (1 až 3 stanoviště pro každého)
            Set<Station> userStations = new HashSet<>();
            if (!allStations.isEmpty()) {
                // Oprava použití Faker random
                int numQuals = faker.random().nextInt(1, Math.min(3, allStations.size()) + 1);
                for (int q = 0; q < numQuals; q++) {
                    userStations.add(allStations.get(faker.random().nextInt(allStations.size())));
                }
            }
            user.setQualifiedStations(userStations);

            // Uložíme uživatele, abychom získali jeho UUID pro další vazby
            user = userRepository.save(user); // Zachytíme uloženého usera s UUID

            // 3. Vytvoření Profilu
            UserProfile profile = new UserProfile();
            profile.setUser(user);
            profile.setFirstName(fname);
            profile.setLastName(lname);
            profile.setPhone(faker.phoneNumber().cellPhone());
            userProfileRepository.save(profile);

            // 4. Vytvoření Smlouvy (Všichni dostanou DPP)
            Contract contract = new Contract();
            contract.setUser(user);
            contract.setType(ContractType.DPP);
            contract.setValidFrom(today.minusMonths(1));
            contractRepository.save(contract);

            // 5. Náhodná Dostupnost (Na příštích 21 dní)
            List<Availability> availabilities = new ArrayList<>();
            for (int d = 0; d < 21; d++) {
                // Mají čas jen s 60% pravděpodobností (zbytek dnů mají volno)
                if (faker.random().nextInt(100) < 60) {
                    Availability a = new Availability();
                    a.setUserId(user.getId());
                    a.setAvailableDate(today.plusDays(d));

                    boolean morning = faker.random().nextBoolean();
                    boolean afternoon = faker.random().nextBoolean();
                    // Pokud padlo false na obojí, dáme mu aspoň ráno
                    if (!morning && !afternoon) morning = true;

                    a.setMorning(morning);
                    a.setAfternoon(afternoon);
                    a.setConfirmed(false);
                    availabilities.add(a);
                }
            }
            availabilityRepository.saveAll(availabilities);

            generatedCount++;
            if (generatedCount % 10 == 0) {
                log.info("Vygenerováno {}/{} brigádníků...", generatedCount, count);
            }
        }

        return "Úspěšně naverbováno " + count + " brigádníků vč. profilů, smluv, kvalifikací a dostupností!";
    }
}