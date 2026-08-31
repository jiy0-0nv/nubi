package com.nubi.domain.account.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserTokenRequest {
    private long userId;
    private String userToken;
    private String newPassword;
}
