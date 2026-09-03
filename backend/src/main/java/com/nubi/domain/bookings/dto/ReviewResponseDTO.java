package com.nubi.domain.bookings.dto;

import com.nubi.entity.ReviewEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "작성한 리뷰 응답")
@Getter
@NoArgsConstructor
public class ReviewResponseDTO {

    @Schema(description = "리뷰 ID", example = "5")
    private Long id;

    @Schema(description = "리뷰가 달린 예약 ID", example = "1")
    private Long bookingId;

    @Schema(description = "리뷰 대상 숙소 ID", example = "1")
    private Long roomId;

    @Schema(description = "평점 (1~5)", example = "5")
    private int rating;

    @Schema(description = "리뷰 내용", example = "바다 전망이 정말 좋았습니다.")
    private String content;

    @Schema(description = "작성 일시", example = "2026-11-13T09:00:00")
    private LocalDateTime createdAt;

    @Builder
    public ReviewResponseDTO(Long id, Long bookingId, Long roomId, int rating, String content, LocalDateTime createdAt) {
        this.id = id;
        this.bookingId = bookingId;
        this.roomId = roomId;
        this.rating = rating;
        this.content = content;
        this.createdAt = createdAt;
    }

    public static ReviewResponseDTO from(ReviewEntity review) {
        return ReviewResponseDTO.builder()
                .id(review.getId())
                .bookingId(review.getBooking().getId())
                .roomId(review.getRoom().getId())
                .rating(review.getRating())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
