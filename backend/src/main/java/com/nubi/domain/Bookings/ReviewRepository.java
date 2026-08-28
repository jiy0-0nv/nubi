package com.nubi.domain.Bookings;

import com.nubi.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {
    boolean existsByBookingId(Long bookingId);
}
