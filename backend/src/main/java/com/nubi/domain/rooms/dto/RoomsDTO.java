package com.nubi.domain.rooms.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.nubi.entity.ReviewEntity;
import com.nubi.entity.RoomImagesEntity;
import com.nubi.entity.RoomsEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

public class RoomsDTO {
    // 1. GET /rooms - 숙소 검색/목록
    @Schema(description = "공개 숙소 목록 항목")
    @Getter
    @Builder
    public static class ListResponse{

        @Schema(description = "숙소 ID. 상세 조회에 사용합니다", example = "1")
        private Long id;

        @Schema(description = "숙소명", example = "해운대 오션뷰 스튜디오")
        private String name;

        @Schema(description = "도시", example = "부산")
        private String city;

        @Schema(description = "국가", example = "대한민국")
        private String country;

        @Schema(description = "평일 1박 요금", example = "80000")
        private BigDecimal weekdayPrice;

        @Schema(description = "주말 1박 요금", example = "120000")
        private BigDecimal weekendPrice;

        @Schema(description = "리뷰 평균 평점. 리뷰가 없으면 0", example = "4.5")
        private double ratingAverage;

        @Schema(description = "대표 사진 경로. 등록된 사진이 없으면 null",
                example = "/uploads/rooms/1/9f3c2a.png")
        private String thumbnailUrl;

        @Schema(description = "체크인 가능 시각", example = "15:00:00")
        private String checkinTime;

        @Schema(description = "최대 수용 인원", example = "4")
        private int maxGuests;

        @Schema(description = "노출 상태", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE"})
        private String status;

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
                .checkinTime(room.getCheckinTime().toString())
                .maxGuests(room.getMaxGuests())
                .status(room.getStatus().name())
                .build();
        }
    }

    //2. GET /rooms/{room_id} - 숙소 상세 조회
    @Schema(description = "공개 숙소 상세 정보")
    @Getter
    @Builder
    public static class DetailResponse{

        @Schema(description = "숙소 ID", example = "1")
        private Long id;

        @Schema(description = "호스트 이름", example = "홍길동")
        private String ownerName;

        @Schema(description = "숙소명", example = "해운대 오션뷰 스튜디오")
        private String name;

        @Schema(description = "숙소 소개글", example = "바다가 보이는 아늑한 스튜디오")
        private String description;

        @Schema(description = "국가", example = "대한민국")
        private String country;

        @Schema(description = "도시", example = "부산")
        private String city;

        @Schema(description = "상세 주소", example = "해운대구 달맞이길 123")
        private String street;

        @Schema(description = "리뷰 평균 평점", example = "4.5")
        private double ratingAverage;

        @Schema(description = "체크인 가능 시각", example = "15:00:00")
        private String checkinTime;

        @Schema(description = "체크아웃 시각", example = "11:00:00")
        private String checkoutTime;

        @Schema(description = "주말 1박 요금", example = "120000")
        private BigDecimal weekendPrice;

        @Schema(description = "평일 1박 요금", example = "80000")
        private BigDecimal weekdayPrice;

        @Schema(description = "최대 수용 인원", example = "4")
        private int maxGuests;

        @Schema(description = "노출 상태", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE"})
        private String status;

        @Schema(description = "등록된 사진 목록. 없으면 빈 배열")
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
    @Schema(description = "숙소 사진")
    @Getter
    @Builder
    public static class ImageResponse {

        @Schema(description = "사진 ID", example = "10")
        private Long id;

        @Schema(description = "정적 서빙 경로", example = "/uploads/rooms/1/9f3c2a.png")
        private String url;

        @Schema(description = "대표 사진 여부", example = "true")
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
    @Schema(description = "숙소 리뷰 항목")
    @Getter
    @Builder
    public static class ReviewResponse {

        @Schema(description = "리뷰 ID", example = "5")
        private Long id;

        @Schema(description = "작성자 이름", example = "김철수")
        private String reviewerName;

        @Schema(description = "평점 (1~5)", example = "5")
        private int rating;

        @Schema(description = "리뷰 내용", example = "바다 전망이 정말 좋았습니다.")
        private String content;

        @Schema(description = "작성 일시", example = "2026-11-13T09:00:00")
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
