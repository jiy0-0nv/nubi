package com.nubi.domain.bookmarks.dto;

import java.time.LocalDateTime;
import com.nubi.entity.BookmarksEntity;

public class BookmarksDTO {
    public record CreateRequest(Long roomId) {}

    public record StatusResponse(boolean bookmarked) {}

    public record ListResponse(
        Long roomId,
        String roomName,
        String city,
        double ratingAverage,
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