package com.nubi.domain.bookings.controller;

import com.nubi.domain.bookings.dto.*;
import com.nubi.domain.bookings.service.BookingsService;
import com.nubi.entity.BookingsEntity;
import com.nubi.global.exception.UnauthenticatedException;
import com.nubi.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingsController {

    private final BookingsService bookingsService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping
    public Page<BookingsResponseDTO> getBookings(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(required = false) BookingsEntity.BookingStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Long userId = requireUserId(authorization);
        return bookingsService.getBookings(userId, status, pageable);
    }

    @PostMapping
    public BookingsResponseDTO createBooking(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody BookingCreateRequestDTO request
    ) {
        Long userId = requireUserId(authorization);
        return bookingsService.createBooking(userId, request);
    }

    @GetMapping("/{bookingId}")
    public BookingsResponseDTO getBookingDetail(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long bookingId
    ) {
        Long userId = requireUserId(authorization);
        return bookingsService.getBookingDetail(userId, bookingId);
    }

    @PatchMapping("/{bookingId}/cancel")
    public BookingsResponseDTO cancelBooking(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long bookingId,
            @RequestBody(required = false) BookingCancelRequestDTO request
    ) {
        Long userId = requireUserId(authorization);
        return bookingsService.cancelBooking(userId, bookingId, request);
    }

    private Long requireUserId(String authorization) {
        Long userId = extractUserId(authorization);
        if (userId == null) {
            throw new UnauthenticatedException();
        }
        return userId;
    }

    private Long extractUserId(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        String token = authorization.substring("Bearer ".length());
        return jwtTokenProvider.parseUserId(token);
    }

    @PostMapping("/{bookingId}/review")
    public ReviewResponseDTO createReview(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long bookingId,
            @RequestBody ReviewCreateRequestDTO request
    ){
        Long userId = requireUserId(authorization);
        return bookingsService.createReview(userId, bookingId, request);
    }

}
