package com.nubi.domain.mypage.dto;

import com.nubi.domain.bookings.dto.BookingsResponseDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Getter
@NoArgsConstructor
public class BookingsSummaryDTO {

    private Long id;
    private String roomName;
    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;
    private String status;

    @Builder
    public BookingsSummaryDTO(Long id, String roomName, LocalDateTime checkInDate, LocalDateTime checkOutDate, String status) {
        this.id = id;
        this.roomName = roomName;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.status = status;
    }

    public static BookingsSummaryDTO from(BookingsResponseDTO full) {
        return BookingsSummaryDTO.builder()
                .id(full.getId())
                .roomName(full.getRoom().getName())  // RoomSummaryDto 안의 실제 getter명에 맞게 수정
                .checkInDate(full.getCheckInDate())
                .checkOutDate(full.getCheckOutDate())
                .status(full.getStatus())
                .build();
    }
}