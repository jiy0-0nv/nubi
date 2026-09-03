package com.nubi.domain.bookmarks.controller;

import com.nubi.domain.bookmarks.dto.BookmarksDTO;
import com.nubi.domain.bookmarks.service.BookmarksService;
import com.nubi.global.exception.UnauthenticatedException;
import com.nubi.security.JwtAuthenticationFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "04. 북마크 (Bookmarks)", description = "관심 숙소 찜하기 / 해제 / 찜 여부 확인 (모두 로그인 필요)")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BookmarksController {

    private final BookmarksService bookmarksService;
    private final HttpServletRequest request;

    @Operation(
            summary = "북마크 추가",
            description = """
                    숙소를 찜 목록에 추가합니다. 응답 본문은 없습니다(201 + 빈 body).

                    같은 숙소를 다시 추가해도 (userId, roomId) 복합키라 중복 저장되지 않습니다.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "북마크 추가 완료", content = @Content),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 숙소", content = @Content)
    })
    @PostMapping("/bookmarks")
    public ResponseEntity<Void> addBookmark(@RequestBody BookmarksDTO.CreateRequest request) {
        Long userId = requireUserId();
        bookmarksService.addBookmark(userId, request.roomId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
            summary = "북마크 해제",
            description = "찜 목록에서 해당 숙소를 제거합니다. 응답 본문은 없습니다(204 + 빈 body).")
    @ApiResponse(responseCode = "204", description = "해제 완료", content = @Content)
    @DeleteMapping("/bookmarks/{roomId}")
    public ResponseEntity<Void> removeBookmark(
            @Parameter(description = "북마크를 해제할 숙소 ID", example = "1")
            @PathVariable Long roomId) {
        Long userId = requireUserId();
        bookmarksService.removeBookmark(userId, roomId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "북마크 여부 확인",
            description = "숙소 상세 화면에서 하트 아이콘의 on/off 상태를 정할 때 사용합니다.")
    @ApiResponse(responseCode = "200", description = "확인 성공")
    @GetMapping("/bookmarks/{roomId}")
    public BookmarksDTO.StatusResponse getBookmarkStatus(
            @Parameter(description = "확인할 숙소 ID", example = "1")
            @PathVariable Long roomId) {
        Long userId = requireUserId();
        return new BookmarksDTO.StatusResponse(bookmarksService.isBookmarked(userId, roomId));
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
