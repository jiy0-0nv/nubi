package com.nubi.domain.admin.dto;

import com.nubi.entity.RoomsEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@NoArgsConstructor
public class AdminRoomResponseDTO {
    private Long id;
    private String name;
    private String description;
    private String country;
    private String city;
    private String street;
    private double ratingAverage;
    private LocalTime checkinTime;
    private LocalTime checkoutTime;
    private BigDecimal weekendPrice;
    private BigDecimal weekdayPrice;
    private int maxGuests;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    public AdminRoomResponseDTO(Long id, String name, String description, String country, String city, String street,
                                 double ratingAverage, LocalTime checkinTime, LocalTime checkoutTime,
                                 BigDecimal weekendPrice, BigDecimal weekdayPrice, int maxGuests, String status,
                                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.country = country;
        this.city = city;
        this.street = street;
        this.ratingAverage = ratingAverage;
        this.checkinTime = checkinTime;
        this.checkoutTime = checkoutTime;
        this.weekendPrice = weekendPrice;
        this.weekdayPrice = weekdayPrice;
        this.maxGuests = maxGuests;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static AdminRoomResponseDTO from(RoomsEntity room) {
        return AdminRoomResponseDTO.builder()
                .id(room.getId())
                .name(room.getName())
                .description(room.getDescription())
                .country(room.getCountry())
                .city(room.getCity())
                .street(room.getStreet())
                .ratingAverage(room.getRatingAverage())
                .checkinTime(room.getCheckinTime())
                .checkoutTime(room.getCheckoutTime())
                .weekendPrice(room.getWeekendPrice())
                .weekdayPrice(room.getWeekdayPrice())
                .maxGuests(room.getMaxGuests())
                .status(room.getStatus().name())
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .build();
    }
}
