package com.nubi.domain.bookings.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "예약 생성 요청. 총 금액은 서버가 계산하므로 보내지 않습니다")
@Getter
@NoArgsConstructor
public class BookingCreateRequestDTO {

    @Schema(description = "예약할 숙소 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long roomId;

    @Schema(description = "체크인 일시 (ISO-8601, 타임존 없음)", example = "2026-11-10T15:00:00",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime checkInDate;

    @Schema(description = "체크아웃 일시. 체크인보다 뒤여야 합니다", example = "2026-11-12T11:00:00",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime checkOutDate;

    @Schema(description = "투숙 인원. 숙소의 maxGuests 를 넘으면 400", example = "2",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private int guestCount;
}
