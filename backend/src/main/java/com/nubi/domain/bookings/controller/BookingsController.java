package com.nubi.domain.bookings.controller;

import com.nubi.domain.bookings.dto.BookingCancelRequestDTO;
import com.nubi.domain.bookings.dto.BookingCreateRequestDTO;
import com.nubi.domain.bookings.dto.BookingsResponseDTO;
import com.nubi.domain.bookings.dto.ReviewCreateRequestDTO;
import com.nubi.domain.bookings.dto.ReviewResponseDTO;
import com.nubi.domain.bookings.service.BookingsService;
import com.nubi.entity.BookingsEntity;
import com.nubi.global.exception.UnauthenticatedException;
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

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingsController {

    private final BookingsService bookingsService;

    @GetMapping
    public Page<BookingsResponseDTO> getBookings(
            @RequestParam(required = false) BookingsEntity.BookingStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Long userId = requireUserId();
        return bookingsService.getBookings(userId, status, pageable);
    }

    @PostMapping
    public BookingsResponseDTO createBooking(@RequestBody BookingCreateRequestDTO request) {
        Long userId = requireUserId();
        return bookingsService.createBooking(userId, request);
    }

    @GetMapping("/{bookingId}")
    public BookingsResponseDTO getBookingDetail(@PathVariable Long bookingId) {
        Long userId = requireUserId();
        return bookingsService.getBookingDetail(userId, bookingId);
    }

    @PatchMapping("/{bookingId}/cancel")
    public BookingsResponseDTO cancelBooking(@PathVariable Long bookingId,
                                             @RequestBody(required = false) BookingCancelRequestDTO request) {
        Long userId = requireUserId();
        return bookingsService.cancelBooking(userId, bookingId, request);
    }

    @PostMapping("/{bookingId}/review")
    public ReviewResponseDTO createReview(@PathVariable Long bookingId, @RequestBody ReviewCreateRequestDTO request) {
        Long userId = requireUserId();
        return bookingsService.createReview(userId, bookingId, request);
    }

    private Long getCurrentUserId() {
        return null; // TODO: SecurityContext 등에서 꺼내오기
    }

    private Long requireUserId() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new UnauthenticatedException();
        }
        return userId;
    }
}