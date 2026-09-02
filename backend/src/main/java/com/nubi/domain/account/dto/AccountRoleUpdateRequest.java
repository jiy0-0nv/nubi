package com.nubi.domain.account.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AccountRoleUpdateRequest {
    private String role; // "USER" | "ADMIN"
}
