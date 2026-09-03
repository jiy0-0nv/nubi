package com.nubi.domain.mypage.dto;

import com.nubi.entity.BookmarksEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "마이페이지용 북마크 요약")
@Getter
@NoArgsConstructor
public class BookmarkSummaryDTO {

    @Schema(description = "찜한 숙소 ID", example = "1")
    private Long roomId;

    @Schema(description = "찜한 숙소명", example = "해운대 오션뷰 스튜디오")
    private String roomName;

    @Schema(description = "찜한 일시", example = "2026-09-01T12:00:00")
    private LocalDateTime bookmarkedAt;

    @Builder
    public BookmarkSummaryDTO(Long roomId, String roomName, LocalDateTime bookmarkedAt) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.bookmarkedAt = bookmarkedAt;
    }

    public static BookmarkSummaryDTO from(BookmarksEntity entity) {
        return BookmarkSummaryDTO.builder()
                .roomId(entity.getRoom().getId())
                .roomName(entity.getRoom().getName())
                .bookmarkedAt(entity.getCreatedAt())
                .build();
    }
}
