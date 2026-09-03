package com.nubi.domain.bookings.dto;

import com.nubi.entity.BookingsEntity;
import com.nubi.entity.ReviewEntity;
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
    /** 이 예약에 이미 남긴 리뷰. 없으면 null — 프런트가 이 값으로 "리뷰 작성폼"과 "내가 남긴 리뷰"를 구분합니다. */
    private ReviewResponseDTO review;

    @Builder
    public BookingsResponseDTO(Long id, RoomSummaryDto room, LocalDateTime checkInDate, LocalDateTime checkOutDate,
                               int guestCount, String status, BigDecimal totalPrice, LocalDateTime cancelledAt,
                               ReviewResponseDTO review) {
        this.id = id;
        this.room = room;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.guestCount = guestCount;
        this.status = status;
        this.totalPrice = totalPrice;
        this.cancelledAt = cancelledAt;
        this.review = review;
    }

    public static BookingsResponseDTO from(BookingsEntity entity) {
        return from(entity, null);
    }

    public static BookingsResponseDTO from(BookingsEntity entity, ReviewEntity review) {
        return BookingsResponseDTO.builder()
                .id(entity.getId())
                .room(RoomSummaryDto.from(entity.getRoom()))
                .checkInDate(entity.getCheckInDate())
                .checkOutDate(entity.getCheckOutDate())
                .guestCount(entity.getGuestCount())
                .status(entity.getStatus().name())
                .totalPrice(entity.getTotalPrice())
                .cancelledAt(entity.getCancelledAt())
                .review(review != null ? ReviewResponseDTO.from(review) : null)
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
