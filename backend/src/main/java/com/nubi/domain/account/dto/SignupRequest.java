package com.nubi.domain.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "회원가입 요청")
@Getter
@NoArgsConstructor
public class SignupRequest {

    @Schema(description = "로그인에 사용할 이메일. 이미 가입된 이메일이면 400", example = "test2@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Schema(description = "비밀번호. BCrypt 로 해싱되어 저장됩니다", example = "testpass123",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @Schema(description = "이름. 아이디 찾기에도 사용됩니다", example = "홍길동",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "전화번호 (하이픈 포함). 아이디 찾기에도 사용됩니다", example = "010-1234-5678",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String phone;
}
