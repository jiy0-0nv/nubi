package com.nubi.domain.bookings;

import com.nubi.entity.BookingsEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookingsService {

    private final BookingsRepository bookingsRepository;

    public Page<BookingResponseDTO> getBookings(Long userId, BookingsEntity.BookingStatus status, Pageable pageable){
        Page<BookingsEntity> bookings = (status == null)
                ? bookingsRepository.findByUserId(userId, pageable)
                : bookingsRepository.findbyUserIdAndStatus(userId, status, pageable);

        return bookings.map(BookingResponseDTO::from);
    }
}
