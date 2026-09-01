package com.nubi.domain.bookings.dto;

import com.nubi.entity.ReviewEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReviewResponseDTO {
    private Long id;
    private Long bookingId;
    private Long roomId;
    private int rating;
    private String content;
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
