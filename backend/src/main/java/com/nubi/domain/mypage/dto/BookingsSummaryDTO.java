package com.nubi.domain.mypage.dto;

import com.nubi.domain.bookings.dto.BookingsResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Schema(description = "마이페이지용 예약 요약. 상세는 GET /api/bookings/{bookingId} 로 조회합니다")
@Getter
@NoArgsConstructor
public class BookingsSummaryDTO {

    @Schema(description = "예약 ID", example = "1")
    private Long id;

    @Schema(description = "예약한 숙소명", example = "해운대 오션뷰 스튜디오")
    private String roomName;

    @Schema(description = "체크인 일시", example = "2026-11-10T15:00:00")
    private LocalDateTime checkInDate;

    @Schema(description = "체크아웃 일시", example = "2026-11-12T11:00:00")
    private LocalDateTime checkOutDate;

    @Schema(description = "예약 상태", example = "CONFIRMED",
            allowableValues = {"CONFIRMED", "COMPLETED", "CANCELLED"})
    private String status;

    @Schema(description = "총 결제 금액", example = "160000")
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
