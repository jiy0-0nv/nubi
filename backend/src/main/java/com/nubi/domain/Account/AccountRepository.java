package com.nubi.domain.Account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.nubi.entity.UserEntity;

@Repository
public interface AccountRepository extends JpaRepository<UserEntity, Long> {
    boolean existsByEmail(String email);
}
