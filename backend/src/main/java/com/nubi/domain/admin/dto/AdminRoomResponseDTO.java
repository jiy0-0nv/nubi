package com.nubi.domain.admin.dto;

import com.nubi.entity.RoomsEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Schema(description = "관리자용 숙소 응답")
@Getter
@NoArgsConstructor
public class AdminRoomResponseDTO {

    @Schema(description = "숙소 ID", example = "1")
    private Long id;

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

    @Schema(description = "리뷰 평균 평점. 리뷰가 없으면 0", example = "4.5")
    private double ratingAverage;

    @Schema(description = "체크인 가능 시각", example = "15:00:00", type = "string")
    private LocalTime checkinTime;

    @Schema(description = "체크아웃 시각", example = "11:00:00", type = "string")
    private LocalTime checkoutTime;

    @Schema(description = "주말 1박 요금", example = "120000")
    private BigDecimal weekendPrice;

    @Schema(description = "평일 1박 요금", example = "80000")
    private BigDecimal weekdayPrice;

    @Schema(description = "최대 수용 인원", example = "4")
    private int maxGuests;

    @Schema(description = "노출 상태", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE"})
    private String status;

    @Schema(description = "등록 일시", example = "2026-09-01T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "최종 수정 일시", example = "2026-09-02T18:00:00")
    private LocalDateTime updatedAt;

    @Builder
    public AdminRoomResponseDTO(Long id, String name, String description, String country, String city, String street,
                                 double ratingAverage, LocalTime checkinTime, LocalTime checkoutTime,
                                 BigDecimal weekendPrice, BigDecimal weekdayPrice, int maxGuests, String status,
                                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.country = country;
        this.city = city;
        this.street = street;
        this.ratingAverage = ratingAverage;
        this.checkinTime = checkinTime;
        this.checkoutTime = checkoutTime;
        this.weekendPrice = weekendPrice;
        this.weekdayPrice = weekdayPrice;
        this.maxGuests = maxGuests;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static AdminRoomResponseDTO from(RoomsEntity room) {
        return AdminRoomResponseDTO.builder()
                .id(room.getId())
                .name(room.getName())
                .description(room.getDescription())
                .country(room.getCountry())
                .city(room.getCity())
                .street(room.getStreet())
                .ratingAverage(room.getRatingAverage())
                .checkinTime(room.getCheckinTime())
                .checkoutTime(room.getCheckoutTime())
                .weekendPrice(room.getWeekendPrice())
                .weekdayPrice(room.getWeekdayPrice())
                .maxGuests(room.getMaxGuests())
                .status(room.getStatus().name())
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .build();
    }
}
