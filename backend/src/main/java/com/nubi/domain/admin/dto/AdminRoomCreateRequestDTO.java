package com.nubi.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@Schema(description = "숙소 등록 요청")
@Getter
@NoArgsConstructor
public class AdminRoomCreateRequestDTO {

    @Schema(description = "숙소명", example = "해운대 오션뷰 스튜디오",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "숙소 소개글", example = "바다가 보이는 아늑한 스튜디오")
    private String description;

    @Schema(description = "국가", example = "대한민국", requiredMode = Schema.RequiredMode.REQUIRED)
    private String country;

    @Schema(description = "도시. 목록 검색의 지역 필터에 사용됩니다", example = "부산",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String city;

    @Schema(description = "상세 주소", example = "해운대구 달맞이길 123",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String street;

    @Schema(description = "체크인 가능 시각 (HH:mm:ss)", example = "15:00:00", type = "string",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalTime checkinTime;

    @Schema(description = "체크아웃 시각 (HH:mm:ss)", example = "11:00:00", type = "string",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalTime checkoutTime;

    @Schema(description = "주말(금·토) 1박 요금", example = "120000",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal weekendPrice;

    @Schema(description = "평일 1박 요금", example = "80000",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal weekdayPrice;

    @Schema(description = "최대 수용 인원. 예약 시 이 값을 넘으면 400", example = "4",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private int maxGuests;
}
