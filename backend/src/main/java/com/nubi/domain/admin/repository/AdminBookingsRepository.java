package com.nubi.domain.admin.repository;

import com.nubi.entity.BookingsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AdminBookingsRepository extends JpaRepository<BookingsEntity, Long> {

    // status/roomId는 선택 필터라, IS NULL 체크로 있을 때만 조건을 건다.
    @Query("SELECT b FROM BookingsEntity b WHERE b.room.owner.id = :ownerId " +
            "AND (:status IS NULL OR b.status = :status) " +
            "AND (:roomId IS NULL OR b.room.id = :roomId)")
    Page<BookingsEntity> findByRoomOwnerId(@Param("ownerId") Long ownerId,
                                            @Param("status") BookingsEntity.BookingStatus status,
                                            @Param("roomId") Long roomId,
                                            Pageable pageable);

    Optional<BookingsEntity> findByIdAndRoom_Owner_Id(Long id, Long ownerId);

    // 엔티티에 세터가 없어 벌크 업데이트로 취소 처리 (bookings 도메인의 updateCancelledStatus와 동일한 방식)
    @Modifying(clearAutomatically = true)
    @Query("UPDATE BookingsEntity b SET b.status = :status, b.cancelledAt = :cancelledAt WHERE b.id = :bookingId")
    void updateCancelledStatus(@Param("bookingId") Long bookingId,
                                @Param("status") BookingsEntity.BookingStatus status,
                                @Param("cancelledAt") LocalDateTime cancelledAt);
}
