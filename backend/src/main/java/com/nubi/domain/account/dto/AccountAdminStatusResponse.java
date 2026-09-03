package com.nubi.domain.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 여부 응답")
public record AccountAdminStatusResponse(

        @Schema(description = "해당 계정의 role 이 ADMIN 이면 true", example = "false")
        boolean admin
) {}
