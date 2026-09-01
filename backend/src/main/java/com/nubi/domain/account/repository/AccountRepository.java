package com.nubi.domain.account.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.nubi.entity.UsersEntity;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<UsersEntity, Long> {
    boolean existsByEmail(String email);
    Optional<UsersEntity> findByEmail(String email);
    Optional<UsersEntity> findByNameAndPhone(String name, String Phone);
}
