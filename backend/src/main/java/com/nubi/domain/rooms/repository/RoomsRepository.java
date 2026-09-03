package com.nubi.domain.rooms.repository;

import com.nubi.entity.RoomsEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoomsRepository extends JpaRepository<RoomsEntity, Long>, JpaSpecificationExecutor<RoomsEntity> {

    // 같은 방에 대한 예약 생성 요청을 직렬화하기 위한 비관적 락 조회.
    // 트랜잭션이 끝날 때까지 이 방 행에 대한 다른 PESSIMISTIC_WRITE 조회는 대기한다.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM RoomsEntity r WHERE r.id = :id")
    Optional<RoomsEntity> findByIdForUpdate(@Param("id") Long id);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE RoomsEntity r SET r.ratingAverage = :ratingAverage where r.id = :roomID")
    void updateRatingAverage(@Param("roomID") Long roomID, @Param("ratingAverage") double ratingAverage);

}
