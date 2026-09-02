package com.nubi.entity;

import com.nubi.entity.converter.RoleConverter;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
public class UsersEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 20)
    private String phone;

    @Convert(converter = RoleConverter.class)
    @Column(nullable = false, length = 10)
    private Role role = Role.USER;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public void changePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
    }

    public void withdraw() {
        this.deletedAt = LocalDateTime.now();
    }

    public void changeRole(Role role) {
        this.role = role;
    }

    @Builder
    public UsersEntity(String email, String passwordHash, String name, String phone, Role role) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.phone = phone;
        this.role = role != null ? role : Role.USER;
    }

    public enum Role {
        USER, ADMIN
    }
}