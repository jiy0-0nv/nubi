package com.nubi.domain.rooms.repository;

import com.nubi.entity.ReviewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {
    Page<ReviewEntity> findByRoomId(Long roomId, Pageable pageable);
    boolean existsByBookingId(Long bookingId);
    Optional<ReviewEntity> findByBookingId(Long bookingId);

    @Query("SELECT AVG(r.rating) FROM ReviewEntity r WHERE r.room.id = :roomId")
    Double findAvgRatingForRoom(@Param("roomId") Long roomId);
}
