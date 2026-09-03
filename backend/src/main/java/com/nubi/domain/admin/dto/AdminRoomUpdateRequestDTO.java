package com.nubi.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@Schema(description = "숙소 부분 수정 요청. 보낸 필드만 변경되고 생략한 필드는 기존 값이 유지됩니다")
@Getter
@NoArgsConstructor
public class AdminRoomUpdateRequestDTO {

    @Schema(description = "숙소명", example = "해운대 오션뷰 스튜디오 (리모델링)")
    private String name;

    @Schema(description = "숙소 소개글", example = "리모델링을 마친 바다 전망 스튜디오")
    private String description;

    @Schema(description = "국가", example = "대한민국")
    private String country;

    @Schema(description = "도시", example = "부산")
    private String city;

    @Schema(description = "상세 주소", example = "해운대구 달맞이길 123")
    private String street;

    @Schema(description = "체크인 가능 시각 (HH:mm:ss)", example = "15:00:00", type = "string")
    private LocalTime checkinTime;

    @Schema(description = "체크아웃 시각 (HH:mm:ss)", example = "11:00:00", type = "string")
    private LocalTime checkoutTime;

    @Schema(description = "주말 1박 요금", example = "130000")
    private BigDecimal weekendPrice;

    @Schema(description = "평일 1박 요금", example = "90000")
    private BigDecimal weekdayPrice;

    @Schema(description = "최대 수용 인원", example = "4")
    private Integer maxGuests;

    @Schema(description = "노출 상태. inactive 로 바꾸면 공개 목록에서 숨겨집니다 (대소문자 무관)",
            example = "inactive", allowableValues = {"active", "inactive"})
    private String status; // "active" | "inactive"
}
