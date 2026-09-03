package com.nubi.domain.admin.repository;

import com.nubi.entity.RoomImagesEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdminRoomImagesRepository extends JpaRepository<RoomImagesEntity, Long> {

    List<RoomImagesEntity> findByRoom_IdOrderBySortOrderAscIdAsc(Long roomId);

    Optional<RoomImagesEntity> findByIdAndRoom_Id(Long id, Long roomId);

    boolean existsByRoom_Id(Long roomId);
}
