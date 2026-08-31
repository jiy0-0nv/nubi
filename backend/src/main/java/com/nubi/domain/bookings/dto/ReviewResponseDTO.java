package com.nubi.domain.bookings.dto;

import com.nubi.entity.ReviewEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ReviewResponseDTO {
    private Long id;
    private Long bookingId;
    private int rating;
    private String content;
    private LocalDateTime createdAt;

    @Builder
    public ReviewResponseDTO(Long id, Long bookingId, int rating, String content, LocalDateTime createdAt) {
        this.id = id;
        this.bookingId = bookingId;
        this.rating = rating;
        this.content = content;
        this.createdAt = createdAt;
    }

    public static ReviewResponseDTO from(ReviewEntity review) {
        return ReviewResponseDTO.builder()
                .id(review.getId())
                .bookingId(review.getBooking().getId())
                .rating(review.getRating())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
