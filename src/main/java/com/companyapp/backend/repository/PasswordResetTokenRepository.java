package com.companyapp.backend.repository;

import com.companyapp.backend.entity.PasswordResetToken;
import com.companyapp.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByUser(User user); // Pro promazání starých tokenů, když si uživatel vyžádá nový
}