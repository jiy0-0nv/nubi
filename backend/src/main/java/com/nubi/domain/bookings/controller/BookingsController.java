package com.nubi.domain.bookings.controller;

import com.nubi.domain.bookings.dto.*;
import com.nubi.domain.bookings.service.BookingsService;
import com.nubi.entity.BookingsEntity;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "03. 예약 (Bookings)", description = "숙소 예약 생성·조회·취소 및 리뷰 작성 (모두 로그인 필요)")
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingsController {

    private final BookingsService bookingsService;
    private final HttpServletRequest request;

    @Operation(
            summary = "내 예약 목록 조회",
            description = "토큰 주인의 예약만 조회됩니다. `status` 로 상태를 필터링할 수 있습니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public Page<BookingsResponseDTO> getBookings(
            @Parameter(description = "예약 상태 필터. 생략하면 전체 조회", example = "CONFIRMED")
            @RequestParam(required = false) BookingsEntity.BookingStatus status,
            @Parameter(description = "페이지 정보 (기본 size=20)")
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Long userId = requireUserId();
        return bookingsService.getBookings(userId, status, pageable);
    }

    @Operation(
            summary = "예약 생성",
            description = """
                    숙소를 예약합니다. 생성된 예약의 상태는 `CONFIRMED` 로 시작합니다.

                    - `totalPrice` 는 평일/주말 요금을 날짜별로 계산해 서버가 산출합니다. 요청으로 보낼 수 없습니다.
                    - 같은 숙소·기간에 요청이 동시에 몰리면 비관적 잠금 충돌로 **409 `BOOKING_LOCK_CONFLICT`** 가 반환됩니다.
                      이 경우 잠시 후 재시도하면 됩니다.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "예약 생성 성공"),
            @ApiResponse(responseCode = "400", description = "날짜가 잘못되었거나 최대 인원 초과, 이미 예약된 기간",
                    content = @Content)
    })
    @PostMapping
    public BookingsResponseDTO createBooking(@RequestBody BookingCreateRequestDTO request) {
        Long userId = requireUserId();
        return bookingsService.createBooking(userId, request);
    }

    @Operation(
            summary = "예약 상세 조회",
            description = "본인 예약만 조회할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않거나 본인 예약이 아님", content = @Content)
    })
    @GetMapping("/{bookingId}")
    public BookingsResponseDTO getBookingDetail(
            @Parameter(description = "예약 ID", example = "1")
            @PathVariable Long bookingId) {
        Long userId = requireUserId();
        return bookingsService.getBookingDetail(userId, bookingId);
    }

    @Operation(
            summary = "예약 취소",
            description = """
                    본인 예약을 취소합니다. row 를 삭제하지 않고 상태를 `CANCELLED` 로 바꾸고
                    `cancelledAt` 을 기록하는 소프트 취소입니다.

                    요청 본문(취소 사유)은 **선택값**이며 생략할 수 있습니다.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "취소 완료"),
            @ApiResponse(responseCode = "404", description = "존재하지 않거나 본인 예약이 아님", content = @Content),
            @ApiResponse(responseCode = "409", description = "이미 취소되었거나 취소할 수 없는 상태", content = @Content)
    })
    @PatchMapping("/{bookingId}/cancel")
    public BookingsResponseDTO cancelBooking(
            @Parameter(description = "취소할 예약 ID", example = "1")
            @PathVariable Long bookingId,
            @RequestBody(required = false) BookingCancelRequestDTO request) {
        Long userId = requireUserId();
        return bookingsService.cancelBooking(userId, bookingId, request);
    }

    @Operation(
            summary = "리뷰 작성",
            description = """
                    숙박이 끝난 예약(`COMPLETED`)에 대해 리뷰를 남깁니다.
                    예약 상태는 체크아웃 시각이 지나면 스케줄러가 자동으로 `COMPLETED` 로 바꿉니다.

                    한 예약당 리뷰는 한 번만 작성할 수 있습니다.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "리뷰 등록 성공"),
            @ApiResponse(responseCode = "400", description = "아직 완료되지 않은 예약이거나 이미 리뷰를 작성함",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "존재하지 않거나 본인 예약이 아님", content = @Content)
    })
    @PostMapping("/{bookingId}/review")
    public ReviewResponseDTO createReview(
            @Parameter(description = "리뷰를 남길 예약 ID", example = "1")
            @PathVariable Long bookingId,
            @RequestBody ReviewCreateRequestDTO request) {
        Long userId = requireUserId();
        return bookingsService.createReview(userId, bookingId, request);
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
