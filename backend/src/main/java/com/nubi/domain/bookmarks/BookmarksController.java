package com.nubi.domain.bookmarks;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
// import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.nubi.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
// @RequestMapping("/bookmarks")    -> 개별 경로로
public class BookmarksController {

    private final BookmarksService bookmarksService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/bookmarks")
    public ResponseEntity<Void> addBookmark(
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @RequestBody BookmarksDTO.CreateRequest request
    ){
        Long userId = extractUserId(authHeader);
        bookmarksService.addBookmark(userId, request.roomId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @DeleteMapping("/bookmarks/{roomId}")
    public ResponseEntity<Void> removeBookmark(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long roomId
    ) {
        Long userId = extractUserId(authHeader);
        bookmarksService.removeBookmark(userId, roomId);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/mypage/bookmarks")
    public ResponseEntity<Page<BookmarksDTO.ListResponse>> getBookmarks(
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        Pageable pageable){
            Long userId = extractUserId(authHeader);
            Page<BookmarksDTO.ListResponse> bookmarks = bookmarksService.getBookmarks(userId, pageable);
            return ResponseEntity.ok(bookmarks);
    }


    // 헤더 누락/형식 오류/토큰 무효 - 전부 인증 실패(401)로 통일해서 응답한다.
    // GlobalExceptionHandler는 건드리지 않고 이 도메인 안에서만 처리.
    private Long extractUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 토큰이 필요합니다.");
        }
        String token = authHeader.substring("Bearer ".length());
        try {
            return jwtTokenProvider.getUserIdFromToken(token);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }
}
