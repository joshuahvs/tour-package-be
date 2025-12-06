package apap.ti._5.tour_package_2306165540_be.model.profile;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "end_user", uniqueConstraints = {
        @UniqueConstraint(name = "uk_end_user_username", columnNames = "username"),
        @UniqueConstraint(name = "uk_end_user_email", columnNames = "email")
})
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_role", length = 40)
public abstract class EndUser {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @Column(name = "email", nullable = false, length = 160)
    private String email;

    @Column(name = "full_name", nullable = false, length = 160)
    private String fullName;

    @Column(name = "gender", length = 20)
    private String gender;

    // Align with existing DB column name 'phone' (was 'phone_number') and allow
    // nullable

    @Column(name = "password_hash", length = 255)
    private String password;

    @Column(name = "organization_name", length = 160)
    private String organizationName;

    @Column(name = "notes", length = 255)
    private String notes;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = true)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onPersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        // updatedAt dibiarkan null saat pertama kali dibuat
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public abstract RoleType getRoleType();
}
