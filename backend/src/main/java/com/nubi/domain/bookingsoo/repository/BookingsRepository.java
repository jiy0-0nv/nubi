package com.nubi.domain.bookingsoo.repository;

import com.nubi.entity.BookingsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface BookingsRepository extends JpaRepository<BookingsEntity, Long> {

    Page<BookingsEntity> findByUserId(Long userId, Pageable pageable);

    Page<BookingsEntity> findByUserIdAndStatus(Long userId, BookingsEntity.BookingStatus status, Pageable pageable);

    // 취소되지 않은 예약 중 같은 방·기간이 겹치는 게 있는지 확인
    @Query("SELECT COUNT(b) > 0 FROM BookingsEntity b " +
            "WHERE b.room.id = :roomId " +
            "AND b.status <> :excludedStatus " +
            "AND b.checkInDate < :checkOutDate AND b.checkOutDate > :checkInDate")
    boolean existsOverlappingBooking(@Param("roomId") Long roomId,
                                      @Param("checkInDate") LocalDateTime checkInDate,
                                      @Param("checkOutDate") LocalDateTime checkOutDate,
                                      @Param("excludedStatus") BookingsEntity.BookingStatus excludedStatus);

    // BookingsEntity에 세터가 없어 엔티티를 직접 못 바꾸므로 벌크 업데이트로 취소 처리
    @Modifying(clearAutomatically = true)
    @Query("UPDATE BookingsEntity b SET b.status = :status, b.cancelledAt = :cancelledAt WHERE b.id = :bookingId")
    void updateCancelledStatus(@Param("bookingId") Long bookingId,
                                @Param("status") BookingsEntity.BookingStatus status,
                                @Param("cancelledAt") LocalDateTime cancelledAt);
}
