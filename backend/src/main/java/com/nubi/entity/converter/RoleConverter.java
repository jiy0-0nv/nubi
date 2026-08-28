package com.nubi.entity.converter;

import com.nubi.entity.UserEntity;
import jakarta.persistence.Converter;

@Converter
public class RoleConverter extends UpperCaseEnumConverter<UserEntity.Role> {
    public RoleConverter() {
        super(UserEntity.Role.class);
    }
}