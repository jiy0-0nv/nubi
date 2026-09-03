package com.nubi.domain.rooms.controller;

import com.nubi.domain.rooms.dto.RoomsDTO;
import com.nubi.domain.rooms.service.RoomsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;

@Tag(name = "02. 숙소 조회 (Rooms)", description = "누구나 호출 가능한 공개 숙소 목록 / 상세 / 리뷰 조회")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rooms")
public class RoomsController {

    private final RoomsService roomsService;

    // 1. GET /rooms
    @Operation(
            summary = "숙소 목록 검색 (공개)",
            description = """
                    조건에 맞는 숙소를 페이지 단위로 조회합니다. **인증 토큰이 필요 없습니다.**

                    모든 검색 조건은 선택값이며, 넘기지 않으면 전체 목록이 조회됩니다.
                    `thumbnailUrl` 은 등록된 대표 사진의 경로이며 사진이 없으면 null 입니다.
                    """)
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public ResponseEntity<Page<RoomsDTO.ListResponse>> getRooms(
        @Parameter(description = "숙소명 / 지역 검색어", example = "해운대")
        @RequestParam(required = false) String keyword,
        @Parameter(description = "체크인 날짜 (yyyy-MM-dd). 해당 기간에 예약 가능한 숙소만 필터링", example = "2026-11-10")
        @RequestParam(required = false) String checkin,
        @Parameter(description = "체크아웃 날짜 (yyyy-MM-dd)", example = "2026-11-12")
        @RequestParam(required = false) String checkout,
        @Parameter(description = "투숙 인원. 최대 수용 인원이 이 값 이상인 숙소만 조회", example = "2")
        @RequestParam(required = false) Integer guests,
        @Parameter(description = "페이지 정보 (page: 0부터, size: 페이지당 개수, sort: 정렬 예 `weekdayPrice,asc`)")
        Pageable pageable
    ){
        Page<RoomsDTO.ListResponse> rooms = roomsService.getRooms(keyword, checkin, checkout, guests, pageable);
        return ResponseEntity.ok(rooms);
    }

    // 2. GET /rooms/{room_id}
    @Operation(
            summary = "숙소 상세 조회 (공개)",
            description = "숙소 기본 정보와 등록된 사진 목록을 함께 반환합니다. **인증 토큰이 필요 없습니다.**")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않거나 삭제된 숙소", content = @Content)
    })
    @GetMapping("/{roomId}")
    public ResponseEntity<RoomsDTO.DetailResponse> getRoomDetail(
            @Parameter(description = "숙소 ID", example = "1")
            @PathVariable Long roomId) {
        RoomsDTO.DetailResponse room = roomsService.getRoomDetail(roomId);
        return ResponseEntity.ok(room);
    }

    // 3. GET /rooms/{room_id}/reviews
    @Operation(
            summary = "숙소 리뷰 목록 조회 (공개)",
            description = "해당 숙소에 등록된 리뷰를 페이지 단위로 반환합니다. **인증 토큰이 필요 없습니다.**")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 숙소", content = @Content)
    })
    @GetMapping("/{roomId}/reviews")
    public ResponseEntity<Page<RoomsDTO.ReviewResponse>> getRoomReviews(
            @Parameter(description = "숙소 ID", example = "1")
            @PathVariable Long roomId,
            @Parameter(description = "페이지 정보 (예: `createdAt,desc` 로 최신순 정렬)")
            Pageable pageable
    ) {
        Page<RoomsDTO.ReviewResponse> reviews = roomsService.getRoomReviews(roomId, pageable);
        return ResponseEntity.ok(reviews);
    }
}
