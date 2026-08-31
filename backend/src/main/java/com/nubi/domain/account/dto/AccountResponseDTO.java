package com.nubi.domain.account.dto;

import com.nubi.entity.UsersEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class AccountResponseDTO {

    private Long id;
    private String email;
    private String name;
    private String phone;
    private String role;
    private LocalDateTime createdAt;

    @Builder
    public AccountResponseDTO(Long id, String email, String name, String phone, String role, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.role = role;
        this.createdAt = createdAt;
    }

    public static AccountResponseDTO from(UsersEntity user) {
        return AccountResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .build();
    }
}