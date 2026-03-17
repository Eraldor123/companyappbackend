package com.companyapp.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class UserQualificationId implements Serializable {

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "qualification_id")
    private Integer qualificationId;

    public UserQualificationId() {}

    public UserQualificationId(UUID userId, Integer qualificationId) {
        this.userId = userId;
        this.qualificationId = qualificationId;
    }

    // Explicitní delegování getterů a setterů
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public Integer getQualificationId() { return qualificationId; }
    public void setQualificationId(Integer qualificationId) { this.qualificationId = qualificationId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserQualificationId)) return false;
        UserQualificationId that = (UserQualificationId) o;
        return Objects.equals(getUserId(), that.getUserId()) &&
                Objects.equals(getQualificationId(), that.getQualificationId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserId(), getQualificationId());
    }
}
