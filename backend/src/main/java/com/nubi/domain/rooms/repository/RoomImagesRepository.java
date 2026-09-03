package com.nubi.domain.rooms.repository;

import com.nubi.entity.RoomImagesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface RoomImagesRepository extends JpaRepository<RoomImagesEntity, Long> {

    List<RoomImagesEntity> findByRoom_IdOrderBySortOrderAscIdAsc(Long roomId);

    List<RoomImagesEntity> findByRoom_IdInAndThumbnailTrue(Collection<Long> roomIds);
}
