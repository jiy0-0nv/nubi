package com.nubi.domain.account.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
public class FindIdRequest {

    private String name;
    private String phone;

}
