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

    @Query("SELECT b FROM BookingsEntity b WHERE b.room.owner.id = :ownerId " +
            "AND (:status IS NULL OR b.status = :status) " +
            "AND (:roomId IS NULL OR b.room.id = :roomId)")
    Page<BookingsEntity> findByRoomOwnerId(@Param("ownerId") Long ownerId,
                                            @Param("status") BookingsEntity.BookingStatus status,
                                            @Param("roomId") Long roomId,
                                            Pageable pageable);

    Optional<BookingsEntity> findByIdAndRoom_Owner_Id(Long id, Long ownerId);

    boolean existsByRoom_Id(Long roomId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE BookingsEntity b SET b.status = :status, b.cancelledAt = :cancelledAt WHERE b.id = :bookingId")
    void updateCancelledStatus(@Param("bookingId") Long bookingId,
                                @Param("status") BookingsEntity.BookingStatus status,
                                @Param("cancelledAt") LocalDateTime cancelledAt);
}
