package com.companyapp.backend.entity;

import com.companyapp.backend.entity.UserQualification;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_qualifications")
@Getter
@Setter
@NoArgsConstructor
public class UserQualification {

    @EmbeddedId
    private UserQualificationId id = new UserQualificationId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("qualificationId")
    @JoinColumn(name = "qualification_id")
    private Qualification qualification;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserQualification)) return false;
        UserQualification that = (UserQualification) o;
        return id!= null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
