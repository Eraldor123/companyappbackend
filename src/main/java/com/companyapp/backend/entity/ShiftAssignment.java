package com.companyapp.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
        name = "shift_assignments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_shift_user", columnNames = {"shift_id", "user_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
public class ShiftAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Oboustranná vazba na Shift, která umožňuje získat všechny přiřazení pro danou směnu. Na straně Shift je definována jako @OneToMany(mappedBy = "shift").
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift shift;

    /**
     * Oboustranná vazba na User, která umožňuje získat všechny přiřazení pro daného uživatele. Na straně User je definována jako @OneToMany(mappedBy = "user").
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShiftAssignment)) return false;
        ShiftAssignment that = (ShiftAssignment) o;
        return id!= null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
