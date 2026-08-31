package com.nubi.domain.admin.repository;

import com.nubi.entity.RoomsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRoomsRepository extends JpaRepository<RoomsEntity, Long> {

    Page<RoomsEntity> findByOwnerId(Long ownerId, Pageable pageable);

    Optional<RoomsEntity> findByIdAndOwnerId(Long id, Long ownerId);
}
