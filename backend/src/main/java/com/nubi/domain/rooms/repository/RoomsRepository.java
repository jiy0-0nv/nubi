package com.nubi.domain.rooms.repository;

import com.nubi.entity.BookingsEntity;
import com.nubi.entity.RoomsEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface RoomsRepository extends JpaRepository<RoomsEntity, Long> {

    // 같은 방에 대한 예약 생성 요청을 직렬화하기 위한 비관적 락 조회.
    // 트랜잭션이 끝날 때까지 이 방 행에 대한 다른 PESSIMISTIC_WRITE 조회는 대기한다.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM RoomsEntity r WHERE r.id = :id")
    Optional<RoomsEntity> findByIdForUpdate(@Param("id") Long id);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE RoomsEntity r SET r.ratingAverage = :ratingAverage where r.id = :roomID")
    void updateRatingAverage(@Param("roomID") Long roomID, @Param("ratingAverage") double ratingAverage);

    @Query("SELECT r FROM RoomsEntity r WHERE " +
            "(:keyword IS NULL OR r.name LIKE CONCAT('%', :keyword, '%') " +
            "  OR r.city LIKE CONCAT('%', :keyword, '%') " +
            "  OR r.country LIKE CONCAT('%', :keyword, '%')) " +
            "AND (:guests IS NULL OR r.maxGuests >= :guests) " +
            "AND (:checkInDate IS NULL OR :checkOutDate IS NULL OR NOT EXISTS (" +
            "  SELECT 1 FROM BookingsEntity b " +
            "  WHERE b.room = r AND b.status <> :cancelledStatus " +
            "  AND FUNCTION('DATE', b.checkInDate) < :checkOutDate " +
            "  AND FUNCTION('DATE', b.checkOutDate) > :checkInDate" +
            "))")
    Page<RoomsEntity> search(@Param("keyword") String keyword,
                              @Param("guests") Integer guests,
                              @Param("checkInDate") LocalDate checkInDate,
                              @Param("checkOutDate") LocalDate checkOutDate,
                              @Param("cancelledStatus") BookingsEntity.BookingStatus cancelledStatus,
                              Pageable pageable);

}
