package com.nubi.domain.bookings.repository;

import com.nubi.domain.bookings.dto.ReviewResponseDTO;
import com.nubi.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewsRepository extends JpaRepository<ReviewEntity, Long> {
    List<ReviewEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
}
