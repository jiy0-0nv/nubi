package com.nubi.entity.converter;

import com.nubi.entity.RoomsEntity;
import jakarta.persistence.Converter;

@Converter
public class RoomStatusConverter extends UpperCaseEnumConverter<RoomsEntity.RoomStatus> {
    public RoomStatusConverter() {
        super(RoomsEntity.RoomStatus.class);
    }
}