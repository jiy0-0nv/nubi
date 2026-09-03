package com.nubi.domain.bookings.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "예약 취소 요청. 본문 전체가 선택값이라 생략하고 호출할 수 있습니다")
@Getter
@NoArgsConstructor
public class BookingCancelRequestDTO {

    @Schema(description = "취소 사유", example = "일정이 변경되었습니다")
    private String reason;
}
