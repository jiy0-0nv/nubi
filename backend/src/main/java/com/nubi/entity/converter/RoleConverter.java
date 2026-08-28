package com.nubi.entity.converter;

import com.nubi.entity.UsersEntity;
import jakarta.persistence.Converter;

@Converter
public class RoleConverter extends UpperCaseEnumConverter<UsersEntity.Role> {
    public RoleConverter() {
        super(UsersEntity.Role.class);
    }
}