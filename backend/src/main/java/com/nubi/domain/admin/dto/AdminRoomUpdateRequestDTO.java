package com.nubi.domain.admin.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@Getter
@NoArgsConstructor
public class AdminRoomUpdateRequestDTO {
    private String name;
    private String description;
    private String country;
    private String city;
    private String street;
    private LocalTime checkinTime;
    private LocalTime checkoutTime;
    private BigDecimal weekendPrice;
    private BigDecimal weekdayPrice;
    private Integer maxGuests;
    private String status; // "active" | "inactive"
}
