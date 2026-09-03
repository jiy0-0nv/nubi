package com.nubi.domain.admin.repository;

import com.nubi.entity.BookingsEntity;
import com.nubi.entity.RoomsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface AdminRoomsRepository extends JpaRepository<RoomsEntity, Long> {

    Page<RoomsEntity> findByOwnerId(Long ownerId, Pageable pageable);

    Optional<RoomsEntity> findByIdAndOwnerId(Long id, Long ownerId);

    @Query("SELECT r FROM RoomsEntity r WHERE r.owner.id = :ownerId " +
            "AND (:keyword IS NULL OR r.name LIKE CONCAT('%', :keyword, '%') " +
            "  OR r.city LIKE CONCAT('%', :keyword, '%') " +
            "  OR r.country LIKE CONCAT('%', :keyword, '%')) " +
            "AND (:guests IS NULL OR r.maxGuests >= :guests) " +
            "AND (:checkInDate IS NULL OR :checkOutDate IS NULL OR NOT EXISTS (" +
            "  SELECT 1 FROM BookingsEntity b " +
            "  WHERE b.room = r AND b.status <> :cancelledStatus " +
            "  AND FUNCTION('DATE', b.checkInDate) < :checkOutDate " +
            "  AND FUNCTION('DATE', b.checkOutDate) > :checkInDate" +
            "))")
    Page<RoomsEntity> search(@Param("ownerId") Long ownerId,
                              @Param("keyword") String keyword,
                              @Param("guests") Integer guests,
                              @Param("checkInDate") LocalDate checkInDate,
                              @Param("checkOutDate") LocalDate checkOutDate,
                              @Param("cancelledStatus") BookingsEntity.BookingStatus cancelledStatus,
                              Pageable pageable);
}
