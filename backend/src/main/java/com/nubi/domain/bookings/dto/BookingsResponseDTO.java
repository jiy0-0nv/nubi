package com.nubi.domain.bookings.dto;

import com.nubi.entity.BookingsEntity;
import com.nubi.entity.ReviewEntity;
import com.nubi.entity.RoomsEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Schema(description = "사용자용 예약 응답")
@Getter
@NoArgsConstructor
public class BookingsResponseDTO {

    @Schema(description = "예약 ID", example = "1")
    private Long id;

    @Schema(description = "예약한 숙소 요약")
    private RoomSummaryDto room;

    @Schema(description = "체크인 일시", example = "2026-11-10T15:00:00")
    private LocalDateTime checkInDate;

    @Schema(description = "체크아웃 일시", example = "2026-11-12T11:00:00")
    private LocalDateTime checkOutDate;

    @Schema(description = "투숙 인원", example = "2")
    private int guestCount;

    @Schema(description = "예약 상태. 체크아웃 시각이 지나면 스케줄러가 COMPLETED 로 변경합니다",
            example = "CONFIRMED", allowableValues = {"CONFIRMED", "COMPLETED", "CANCELLED"})
    private String status;

    @Schema(description = "총 결제 금액 (서버 계산값)", example = "160000")
    private BigDecimal totalPrice;

    @Schema(description = "취소 일시. 취소되지 않았으면 null", example = "2026-09-02T09:00:00")
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


    @Schema(description = "예약한 숙소 요약 정보")
    @Getter
    @NoArgsConstructor
    public static class RoomSummaryDto {

        @Schema(description = "숙소 ID", example = "1")
        private Long id;

        @Schema(description = "숙소명", example = "해운대 오션뷰 스튜디오")
        private String name;

        @Schema(description = "도시", example = "부산")
        private String city;

        @Schema(description = "숙소 소개글", example = "바다가 보이는 아늑한 스튜디오")
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
