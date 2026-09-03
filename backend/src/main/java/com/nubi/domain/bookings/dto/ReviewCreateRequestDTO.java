package com.nubi.domain.bookings.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "리뷰 작성 요청")
@Getter
@NoArgsConstructor
public class ReviewCreateRequestDTO {

    @Schema(description = "평점 (1~5). 숙소의 ratingAverage 계산에 반영됩니다", example = "5",
            minimum = "1", maximum = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    private int rating;

    @Schema(description = "리뷰 내용", example = "바다 전망이 정말 좋았습니다. 다음에 또 오고 싶어요.",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;
}
