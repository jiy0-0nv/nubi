package com.nubi.domain.account.dto;

import com.nubi.entity.UsersEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "계정 정보 응답 (비밀번호는 포함되지 않습니다)")
@Getter
@NoArgsConstructor
public class AccountResponseDTO {

    @Schema(description = "사용자 ID", example = "23")
    private Long id;

    @Schema(description = "이메일", example = "test2@example.com")
    private String email;

    @Schema(description = "이름", example = "홍길동")
    private String name;

    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phone;

    @Schema(description = "권한", example = "USER", allowableValues = {"USER", "ADMIN"})
    private String role;

    @Schema(description = "가입 일시", example = "2026-09-01T10:30:00")
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
