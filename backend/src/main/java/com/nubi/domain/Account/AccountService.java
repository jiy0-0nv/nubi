package com.nubi.domain.Account;

import com.nubi.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    @Transactional
    public Long signup(SignupRequest request) {
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        UserEntity user = UserEntity.builder()
                .email(request.getEmail())
                .passwordHash(request.getPassword())
                .name(request.getName())
                .phone(request.getPhone())
                .build();

        return accountRepository.save(user).getId();
    }
}