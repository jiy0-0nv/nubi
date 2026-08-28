package com.nubi.domain.Bookings;

import com.nubi.entity.BookingsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingsRepository extends JpaRepository<BookingsEntity, Long> {

    Page<BookingsEntity> findByUserId(Long userId, Pageable pageable);

    //이대로 찾아지는가?
    Page<BookingsEntity> findbyUserIdAndStatus(Long userId, BookingsEntity.BookingStatus status, Pageable pageable);
}
