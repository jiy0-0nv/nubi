package com.nubi.domain.admin.dto;

import com.nubi.entity.BookingsEntity;
import com.nubi.entity.RoomsEntity;
import com.nubi.entity.UsersEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class AdminBookingResponseDTO {

    private Long id;
    private RoomSummary room;
    private GuestSummary guest;
    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;
    private int guestCount;
    private String status;
    private BigDecimal totalPrice;
    private LocalDateTime cancelledAt;
    private LocalDateTime createdAt;

    @Builder
    public AdminBookingResponseDTO(Long id, RoomSummary room, GuestSummary guest, LocalDateTime checkInDate,
                                    LocalDateTime checkOutDate, int guestCount, String status,
                                    BigDecimal totalPrice, LocalDateTime cancelledAt, LocalDateTime createdAt) {
        this.id = id;
        this.room = room;
        this.guest = guest;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.guestCount = guestCount;
        this.status = status;
        this.totalPrice = totalPrice;
        this.cancelledAt = cancelledAt;
        this.createdAt = createdAt;
    }

    public static AdminBookingResponseDTO from(BookingsEntity booking) {
        return AdminBookingResponseDTO.builder()
                .id(booking.getId())
                .room(RoomSummary.from(booking.getRoom()))
                .guest(GuestSummary.from(booking.getUser()))
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .guestCount(booking.getGuestCount())
                .status(booking.getStatus().name())
                .totalPrice(booking.getTotalPrice())
                .cancelledAt(booking.getCancelledAt())
                .createdAt(booking.getCreatedAt())
                .build();
    }

    @Getter
    @NoArgsConstructor
    public static class RoomSummary {
        private Long id;
        private String name;
        private String city;

        @Builder
        public RoomSummary(Long id, String name, String city) {
            this.id = id;
            this.name = name;
            this.city = city;
        }

        public static RoomSummary from(RoomsEntity room) {
            return RoomSummary.builder()
                    .id(room.getId())
                    .name(room.getName())
                    .city(room.getCity())
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    public static class GuestSummary {
        private Long id;
        private String name;
        private String email;

        @Builder
        public GuestSummary(Long id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        public static GuestSummary from(UsersEntity user) {
            return GuestSummary.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .build();
        }
    }
}
