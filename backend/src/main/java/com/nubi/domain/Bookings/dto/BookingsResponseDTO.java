package com.nubi.domain.Bookings.dto;

import com.nubi.entity.BookingsEntity;
import com.nubi.entity.RoomsEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class BookingsResponseDTO {

    private Long id;
    private RoomSummaryDto room;
    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;
    private int guestCount;
    private String status;
    private BigDecimal totalprice;
    private LocalDateTime cancelledAt;

    @Builder
    public BookingsResponseDTO(Long id, RoomSummaryDto room, LocalDateTime checkInDate, LocalDateTime checkOutDate,
                               int guestCount, String status, BigDecimal totalprice, LocalDateTime cancelledAt) {
        this.id = id;
        this.room = room;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.guestCount = guestCount;
        this.status = status;
        this.totalprice = totalprice;
        this.cancelledAt = cancelledAt;
    }

    public static BookingsResponseDTO from(BookingsEntity booking) {
        return BookingsResponseDTO.builder()
                .id(booking.getId())
                .room(RoomSummaryDto.from(booking.getRoom()))
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .guestCount(booking.getGuestCount())
                .status(booking.getStatus().name())
                .totalprice(booking.getTotalPrice())
                .cancelledAt(booking.getCancelledAt())
                .build();
    }

    @Getter
    @NoArgsConstructor
    public static class RoomSummaryDto {
        private Long id;
        private String name;
        private String city;
        private String description;

        @Builder
        public RoomSummaryDto(Long id, String name, String city, String description) {
            this.id = id;
            this.name = name;
            this.city = city;
            this.description = description;
        }

        public static RoomSummaryDto from(RoomsEntity room) {
            return RoomSummaryDto.builder()
                    .id(room.getId())
                    .name(room.getName())
                    .city(room.getCity())
                    .description(room.getDescription())
                    .build();
        }
    }
}
