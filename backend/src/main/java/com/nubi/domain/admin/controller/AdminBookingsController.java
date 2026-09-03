package com.nubi.domain.admin.controller;

import com.nubi.domain.admin.dto.AdminBookingResponseDTO;
import com.nubi.domain.admin.service.AdminBookingsService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "07. 관리자 - 예약 (Admin Bookings)",
     description = """
             호스트가 **자신의 숙소에 들어온 예약**을 관리합니다.
             남의 숙소 예약에 접근하면 403 입니다.
             """)
@RestController
@RequestMapping("/api/admin/bookings")
@RequiredArgsConstructor
public class AdminBookingsController {

    private final AdminBookingsService adminBookingsService;
    private final HttpServletRequest request;

    @Operation(
            summary = "내 숙소 예약 목록 조회",
            description = """
                    토큰 주인이 owner 인 숙소들의 예약을 페이지 단위로 조회합니다.
                    `status` 와 `room_id` 로 필터링할 수 있으며 둘 다 선택값입니다.
                    """)
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public Page<AdminBookingResponseDTO> getBookings(
            @Parameter(description = "예약 상태 필터. 생략하면 전체 조회", example = "CONFIRMED")
            @RequestParam(required = false) BookingsEntity.BookingStatus status,
            @Parameter(description = "특정 숙소의 예약만 조회 (쿼리 파라미터 이름은 `room_id`)", example = "1")
            @RequestParam(name = "room_id", required = false) Long roomId,
            @Parameter(description = "페이지 정보 (기본 size=20)")
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Long ownerId = requireUserId();
        return adminBookingsService.getBookings(ownerId, status, roomId, pageable);
    }

    @Operation(
            summary = "예약 상세 조회",
            description = "예약자 정보(`guest`)와 숙소 요약(`room`)을 함께 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 예약", content = @Content)
    })
    @GetMapping("/{bookingId}")
    public AdminBookingResponseDTO getBookingDetail(
            @Parameter(description = "예약 ID", example = "1")
            @PathVariable Long bookingId) {
        Long ownerId = requireUserId();
        return adminBookingsService.getBookingDetail(ownerId, bookingId);
    }

    @Operation(
            summary = "예약 취소 (호스트)",
            description = """
                    호스트가 예약을 취소합니다. row 를 삭제하지 않고 상태를 `CANCELLED` 로 바꾸는 소프트 취소입니다.
                    이미 취소된 예약을 다시 취소하면 409 입니다. 응답 본문은 없습니다.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "취소 완료", content = @Content),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 예약", content = @Content),
            @ApiResponse(responseCode = "409", description = "이미 취소된 예약", content = @Content)
    })
    @DeleteMapping("/{bookingId}")
    public void cancelBooking(
            @Parameter(description = "취소할 예약 ID", example = "1")
            @PathVariable Long bookingId) {
        Long ownerId = requireUserId();
        adminBookingsService.cancelBooking(ownerId, bookingId);
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
