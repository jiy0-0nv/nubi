package com.nubi.domain.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "아이디(이메일) 찾기 요청. 이름과 전화번호가 모두 일치해야 합니다")
@Getter
@NoArgsConstructor
public class FindIdRequest {

    @Schema(description = "가입 시 입력한 이름", example = "홍길동",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "가입 시 입력한 전화번호", example = "010-1234-5678",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String phone;
}
