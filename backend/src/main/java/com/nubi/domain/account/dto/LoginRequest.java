package com.nubi.domain.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "로그인 요청")
@Getter
@NoArgsConstructor
public class LoginRequest {

    @Schema(description = "가입한 이메일", example = "test2@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Schema(description = "비밀번호", example = "testpass123",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
