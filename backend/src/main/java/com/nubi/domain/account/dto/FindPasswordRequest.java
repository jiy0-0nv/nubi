package com.nubi.domain.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "비밀번호 재설정 메일 발송 요청")
@Getter
@NoArgsConstructor
public class FindPasswordRequest {

    @Schema(description = "재설정 토큰을 받을 가입 이메일", example = "test2@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;
}
