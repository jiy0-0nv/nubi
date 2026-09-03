package com.nubi.domain.admin.dto;

import com.nubi.entity.BookingsEntity;
import com.nubi.entity.RoomsEntity;
import com.nubi.entity.UsersEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "관리자용 예약 응답. 예약자 정보(guest)가 함께 포함됩니다")
@Getter
@NoArgsConstructor
public class AdminBookingResponseDTO {

    @Schema(description = "예약 ID", example = "1")
    private Long id;

    @Schema(description = "예약된 숙소 요약")
    private RoomSummary room;

    @Schema(description = "예약자 요약")
    private GuestSummary guest;

    @Schema(description = "체크인 일시", example = "2026-11-10T15:00:00")
    private LocalDateTime checkInDate;

    @Schema(description = "체크아웃 일시", example = "2026-11-12T11:00:00")
    private LocalDateTime checkOutDate;

    @Schema(description = "투숙 인원", example = "2")
    private int guestCount;

    @Schema(description = "예약 상태", example = "CONFIRMED",
            allowableValues = {"CONFIRMED", "COMPLETED", "CANCELLED"})
    private String status;

    @Schema(description = "총 결제 금액. 평일/주말 요금을 날짜별로 합산한 값입니다", example = "160000")
    private BigDecimal totalPrice;

    @Schema(description = "취소 일시. 취소되지 않았으면 null", example = "2026-09-02T09:00:00")
    private LocalDateTime cancelledAt;

    @Schema(description = "예약 생성 일시", example = "2026-09-01T12:00:00")
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

    @Schema(description = "예약된 숙소 요약 정보")
    @Getter
    @NoArgsConstructor
    public static class RoomSummary {

        @Schema(description = "숙소 ID", example = "1")
        private Long id;

        @Schema(description = "숙소명", example = "해운대 오션뷰 스튜디오")
        private String name;

        @Schema(description = "도시", example = "부산")
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

    @Schema(description = "예약자 요약 정보")
    @Getter
    @NoArgsConstructor
    public static class GuestSummary {

        @Schema(description = "예약자 사용자 ID", example = "24")
        private Long id;

        @Schema(description = "예약자 이름", example = "김철수")
        private String name;

        @Schema(description = "예약자 이메일", example = "guest@example.com")
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
