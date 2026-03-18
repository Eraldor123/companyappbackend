package com.companyapp.backend.repository;

import com.companyapp.backend.entity.UserQualification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface UserQualificationRepository extends JpaRepository<UserQualification, UUID> {

    // TENTO ŘÁDEK VYŘEŠÍ VAŠI CHYBU - Spring Data JPA automaticky pochopí, co má hledat
    List<UserQualification> findByUserId(UUID userId);

    // Rovnou si sem můžeme přidat i metodu pro mazání odebraných kvalifikací,
    // kterou máme ve službě zatím zakomentovanou
    void deleteAllByUserIdAndQualificationIdIn(UUID user_id, Collection<Integer> qualification_id);
}