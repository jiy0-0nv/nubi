package com.nubi.domain.rooms;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.nubi.entity.ReviewEntity;
import com.nubi.entity.RoomImagesEntity;
import com.nubi.entity.RoomsEntity;

import lombok.Builder;
import lombok.Getter;

public class RoomsDTO {
    // 1. GET /rooms - 숙소 검색/목록
    @Getter
    @Builder
    public static class ListResponse{
        private Long id;
        private String name;
        private String city;
        private String country;
        private BigDecimal weekdayPrice;
        private BigDecimal weekendPrice;
        private double ratingAverage;
        private String thumbnailUrl;

        public static ListResponse from(RoomsEntity room, String thumbnailUrl){
            return ListResponse.builder()
                .id(room.getId())
                .name(room.getName())
                .city(room.getCity())
                .country(room.getCountry())
                .weekdayPrice(room.getWeekdayPrice())
                .weekendPrice(room.getWeekendPrice())
                .ratingAverage(room.getRatingAverage())
                .thumbnailUrl(thumbnailUrl)
                .build();
        }
    }

    //2. GET /rooms/{room_id} - 숙소 상세 조회
    @Getter
    @Builder
    public static class DetailResponse{
        private Long id;
        private String ownerName;
        private String name;
        private String description;
        private String country;
        private String city;
        private String street;
        private double ratingAverage;
        private String checkinTime;
        private String checkoutTime;
        private BigDecimal weekendPrice;
        private BigDecimal weekdayPrice;
        private int maxGuests;
        private String status;
        private List<ImageResponse> images;

        public static DetailResponse from(RoomsEntity room, List<RoomImagesEntity> images) {
            return DetailResponse.builder()
                .id(room.getId())
                .ownerName(room.getOwner().getName())
                .name(room.getName())
                .description(room.getDescription())
                .country(room.getCountry())
                .city(room.getCity())
                .street(room.getStreet())
                .ratingAverage(room.getRatingAverage())
                .checkinTime(room.getCheckinTime().toString())
                .checkoutTime(room.getCheckoutTime().toString())
                .weekendPrice(room.getWeekendPrice())
                .weekdayPrice(room.getWeekdayPrice())
                .maxGuests(room.getMaxGuests())
                .status(room.getStatus().name())
                .images(images.stream().map(ImageResponse::from).toList())
                .build();
        }
    }

    // 숙소 사진
    @Getter
    @Builder
    public static class ImageResponse {
        private Long id;
        private String url;
        private boolean thumbnail;

        public static ImageResponse from(RoomImagesEntity image) {
            return ImageResponse.builder()
                .id(image.getId())
                .url(image.getUrl())
                .thumbnail(image.isThumbnail())
                .build();
        }
    }

    // 3. GET /rooms/{room_id}/reviews - 특정 숙소의 리뷰 목록
    @Getter
    @Builder
    public static class ReviewResponse {
        private Long id;
        private String reviewerName;
        private int rating;
        private String content;
        private LocalDateTime createdAt;

        public static ReviewResponse from(ReviewEntity review) {
            return ReviewResponse.builder()
                .id(review.getId())
                .reviewerName(review.getUser().getName())
                .rating(review.getRating())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .build();
        }
    }
}
