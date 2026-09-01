package com.nubi.domain.admin.controller;

import com.nubi.domain.admin.dto.AdminBookingResponseDTO;
import com.nubi.domain.admin.service.AdminBookingsService;
import com.nubi.entity.BookingsEntity;
import com.nubi.global.exception.UnauthenticatedException;
import com.nubi.security.JwtAuthenticationFilter;
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

@RestController
@RequestMapping("/api/admin/bookings")
@RequiredArgsConstructor
public class AdminBookingsController {

    private final AdminBookingsService adminBookingsService;
    private final HttpServletRequest request;

    @GetMapping
    public Page<AdminBookingResponseDTO> getBookings(
            @RequestParam(required = false) BookingsEntity.BookingStatus status,
            @RequestParam(name = "room_id", required = false) Long roomId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Long ownerId = requireUserId();
        return adminBookingsService.getBookings(ownerId, status, roomId, pageable);
    }

    @GetMapping("/{bookingId}")
    public AdminBookingResponseDTO getBookingDetail(@PathVariable Long bookingId) {
        Long ownerId = requireUserId();
        return adminBookingsService.getBookingDetail(ownerId, bookingId);
    }

    @DeleteMapping("/{bookingId}")
    public void cancelBooking(@PathVariable Long bookingId) {
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
