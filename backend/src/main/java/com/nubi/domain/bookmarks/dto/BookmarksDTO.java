package com.nubi.domain.bookmarks.dto;

import java.time.LocalDateTime;
import com.nubi.entity.BookmarksEntity;
import io.swagger.v3.oas.annotations.media.Schema;

public class BookmarksDTO {

    @Schema(description = "북마크 추가 요청")
    public record CreateRequest(

            @Schema(description = "찜할 숙소 ID", example = "1",
                    requiredMode = Schema.RequiredMode.REQUIRED)
            Long roomId
    ) {}

    @Schema(description = "북마크 여부 응답")
    public record StatusResponse(

            @Schema(description = "이미 찜한 숙소면 true", example = "true")
            boolean bookmarked
    ) {}

    @Schema(description = "북마크 목록 항목")
    public record ListResponse(

        @Schema(description = "숙소 ID", example = "1")
        Long roomId,

        @Schema(description = "숙소명", example = "해운대 오션뷰 스튜디오")
        String roomName,

        @Schema(description = "도시", example = "부산")
        String city,

        @Schema(description = "리뷰 평균 평점", example = "4.5")
        double ratingAverage,

        @Schema(description = "찜한 일시", example = "2026-09-01T12:00:00")
        LocalDateTime bookmarkedAt
    ){
        public static ListResponse from(BookmarksEntity bookmark){
            return new ListResponse(
                bookmark.getRoom().getId(),
                bookmark.getRoom().getName(),
                bookmark.getRoom().getCity(),
                bookmark.getRoom().getRatingAverage(),
                bookmark.getCreatedAt()
            );
        }
    }
}
