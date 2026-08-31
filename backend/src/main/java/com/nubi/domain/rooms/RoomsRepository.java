package com.nubi.domain.rooms;

import com.nubi.entity.RoomsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomsRepository extends JpaRepository<RoomsEntity, Long> {
}
