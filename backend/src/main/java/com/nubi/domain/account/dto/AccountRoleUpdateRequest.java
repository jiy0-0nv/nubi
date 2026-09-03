package com.nubi.domain.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "권한 변경 요청")
@Getter
@NoArgsConstructor
public class AccountRoleUpdateRequest {

    @Schema(description = "변경할 권한. USER 또는 ADMIN 만 허용되며 그 외 값은 400",
            example = "ADMIN", allowableValues = {"USER", "ADMIN"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String role; // "USER" | "ADMIN"
}
