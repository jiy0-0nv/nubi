package com.nubi.domain.mypage.dto;

import com.nubi.entity.BookmarksEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class BookmarkSummaryDTO {

    private Long roomId;
    private String roomName;
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