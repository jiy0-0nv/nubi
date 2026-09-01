package com.nubi.domain.bookings.scheduler;

import com.nubi.domain.bookings.repository.BookingsRepository;
import com.nubi.entity.BookingsEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor

public class BookingCompletionScheduler {
    private final BookingsRepository bookingsRepository;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void completeBookings() {
        bookingsRepository.completeBookings(
                BookingsEntity.BookingStatus.COMPLETED,
                BookingsEntity.BookingStatus.CONFIRMED,
                LocalDateTime.now());
    }
}
