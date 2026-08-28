package com.nubi.domain.bookings;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingsController {

    private final BookingsService bookingsService;

    @GetMapping
    public Page<?> getBookings(@PageableDefault(size = 20) Pageable pageable) {
        Long userId = null;
        return bookingsService.getBookings(userId, pageable);
    }
}
