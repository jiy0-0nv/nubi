package com.nubi.domain.mypage.dto;

import com.nubi.domain.bookings.dto.BookingsResponseDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter
@NoArgsConstructor
public class BookingsSummaryDTO {

    private Long id;
    private String roomName;
    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;
    private String status;
    private BigDecimal totalPrice;

    @Builder
    public BookingsSummaryDTO(Long id, String roomName, LocalDateTime checkInDate, LocalDateTime checkOutDate,
                              String status, BigDecimal totalPrice) {
        this.id = id;
        this.roomName = roomName;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.status = status;
        this.totalPrice = totalPrice;
    }

    public static BookingsSummaryDTO from(BookingsResponseDTO full) {
        return BookingsSummaryDTO.builder()
                .id(full.getId())
                .roomName(full.getRoom() != null ? full.getRoom().getName() : null)
                .checkInDate(full.getCheckInDate())
                .checkOutDate(full.getCheckOutDate())
                .status(full.getStatus())
                .totalPrice(full.getTotalPrice())
                .build();
    }
}