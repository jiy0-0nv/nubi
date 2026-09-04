package com.nubi.domain.mypage.dto;

import com.nubi.entity.BookmarksEntity;
import com.nubi.entity.RoomsEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "마이페이지용 북마크 요약. /rooms 목록 카드를 그대로 그릴 수 있도록 숙소 정보를 함께 내려줍니다.")
@Getter
@NoArgsConstructor
public class BookmarkSummaryDTO {

    @Schema(description = "찜한 숙소 ID", example = "1")
    private Long roomId;

    @Schema(description = "찜한 숙소명", example = "해운대 오션뷰 스튜디오")
    private String roomName;

    @Schema(description = "찜한 일시", example = "2026-09-01T12:00:00")
    private LocalDateTime bookmarkedAt;

    @Schema(description = "대표 사진 URL. 없으면 null", example = "/uploads/rooms/1/abc.jpg")
    private String thumbnailUrl;

    @Schema(description = "국가", example = "대한민국")
    private String country;

    @Schema(description = "도시", example = "부산")
    private String city;

    @Schema(description = "최대 인원", example = "4")
    private int maxGuests;

    @Schema(description = "평균 평점", example = "4.5")
    private double ratingAverage;

    @Schema(description = "평일 1박 요금", example = "150000")
    private BigDecimal weekdayPrice;

    @Schema(description = "주말 1박 요금", example = "200000")
    private BigDecimal weekendPrice;

    @Schema(description = "숙소 상태", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE"})
    private String status;

    @Builder
    public BookmarkSummaryDTO(Long roomId, String roomName, LocalDateTime bookmarkedAt, String thumbnailUrl,
                              String country, String city, int maxGuests, double ratingAverage,
                              BigDecimal weekdayPrice, BigDecimal weekendPrice, String status) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.bookmarkedAt = bookmarkedAt;
        this.thumbnailUrl = thumbnailUrl;
        this.country = country;
        this.city = city;
        this.maxGuests = maxGuests;
        this.ratingAverage = ratingAverage;
        this.weekdayPrice = weekdayPrice;
        this.weekendPrice = weekendPrice;
        this.status = status;
    }

    public static BookmarkSummaryDTO from(BookmarksEntity entity, String thumbnailUrl) {
        RoomsEntity room = entity.getRoom();
        return BookmarkSummaryDTO.builder()
                .roomId(room.getId())
                .roomName(room.getName())
                .bookmarkedAt(entity.getCreatedAt())
                .thumbnailUrl(thumbnailUrl)
                .country(room.getCountry())
                .city(room.getCity())
                .maxGuests(room.getMaxGuests())
                .ratingAverage(room.getRatingAverage())
                .weekdayPrice(room.getWeekdayPrice())
                .weekendPrice(room.getWeekendPrice())
                .status(room.getStatus().name())
                .build();
    }
}
