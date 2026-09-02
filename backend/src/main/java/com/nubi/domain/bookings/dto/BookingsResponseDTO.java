package com.nubi.domain.bookings.dto;

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
    private BigDecimal totalPrice;
    private LocalDateTime cancelledAt;

    @Builder
    public BookingsResponseDTO(Long id, RoomSummaryDto room, LocalDateTime checkInDate, LocalDateTime checkOutDate,
                               int guestCount, String status, BigDecimal totalPrice, LocalDateTime cancelledAt) {
        this.id = id;
        this.room = room;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.guestCount = guestCount;
        this.status = status;
        this.totalPrice = totalPrice;
        this.cancelledAt = cancelledAt;
    }

    public static BookingsResponseDTO from(BookingsEntity entity) {
        return BookingsResponseDTO.builder()
                .id(entity.getId())
                .room(RoomSummaryDto.from(entity.getRoom()))
                .checkInDate(entity.getCheckInDate())
                .checkOutDate(entity.getCheckOutDate())
                .guestCount(entity.getGuestCount())
                .status(entity.getStatus().name())
                .totalPrice(entity.getTotalPrice())
                .cancelledAt(entity.getCancelledAt())
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
