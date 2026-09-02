package com.nubi.domain.bookmarks;

import com.nubi.global.exception.UnauthenticatedException;
import com.nubi.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
// @RequestMapping("/bookmarks")    -> 개별 경로로
public class BookmarksController {

    private final BookmarksService bookmarksService;
    private final HttpServletRequest request;

    @PostMapping("/bookmarks")
    public ResponseEntity<Void> addBookmark(@RequestBody BookmarksDTO.CreateRequest request) {
        Long userId = requireUserId();
        bookmarksService.addBookmark(userId, request.roomId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @DeleteMapping("/bookmarks/{roomId}")
    public ResponseEntity<Void> removeBookmark(@PathVariable Long roomId) {
        Long userId = requireUserId();
        bookmarksService.removeBookmark(userId, roomId);
        return ResponseEntity.noContent().build();
    }

    private Long getCurrentUserId() {
        Object userId = request.getAttribute(JwtAuthenticationFilter.USER_ID_ATTRIBUTE);
        return userId instanceof Long ? (Long) userId : null;
    }

    private Long requireUserId() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new UnauthenticatedException();
        }
        return userId;
    }
}
