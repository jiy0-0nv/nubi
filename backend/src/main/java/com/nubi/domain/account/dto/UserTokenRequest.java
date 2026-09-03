package com.nubi.domain.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "비밀번호 재설정 요청. Authorization 헤더가 아니라 본문의 토큰을 사용합니다")
@Getter
@NoArgsConstructor
public class UserTokenRequest {

    @Schema(description = "비밀번호 찾기 메일로 받은 재설정 토큰 (유효기간 30분)",
            example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIyMyJ9.xxxxx",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String userToken;

    @Schema(description = "새로 설정할 비밀번호", example = "newpass1234",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String newPassword;
}
