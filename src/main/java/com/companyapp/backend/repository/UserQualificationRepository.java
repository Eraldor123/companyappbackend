package com.companyapp.backend.repository;

import com.companyapp.backend.entity.UserQualification;
import com.companyapp.backend.entity.keys.UserQualificationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserQualificationRepository extends JpaRepository<UserQualification, UserQualificationId> {

    List<UserQualification> findByIdUserId(UUID userId);

    List<UserQualification> findByIdQualificationId(Integer qualificationId);
}