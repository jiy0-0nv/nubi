package com.nubi.entity.converter;

import com.nubi.entity.BookingsEntity;
import jakarta.persistence.Converter;

@Converter
public class BookingStatusConverter extends UpperCaseEnumConverter<BookingsEntity.BookingStatus> {
    public BookingStatusConverter() {
        super(BookingsEntity.BookingStatus.class);
    }
}