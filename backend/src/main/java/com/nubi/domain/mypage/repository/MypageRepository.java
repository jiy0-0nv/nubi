package com.nubi.domain.mypage.repository;

import com.nubi.domain.mypage.dto.MypageResponseDTO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MypageRepository extends JpaRepository<MypageResponseDTO, Long> {
}
