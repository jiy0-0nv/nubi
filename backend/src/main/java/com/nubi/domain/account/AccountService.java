package com.nubi.domain.account;

import com.nubi.domain.account.dto.LoginRequest;
import com.nubi.domain.account.dto.SignupRequest;
import com.nubi.entity.UsersEntity;
import com.nubi.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;


    //signup() 회원가입
    @Transactional
    public Long signup(SignupRequest request) {
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        UsersEntity user = UsersEntity.builder()
                .email(request.getEmail())
                .passwordHash(request.getPassword())
                .name(request.getName())
                .phone(request.getPhone())
                .build();

        return accountRepository.save(user).getId();
    }


    // login() 토큰 발급해서 리턴해줘야함...
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional(readOnly = true)
    public String login(LoginRequest request) {
        UsersEntity user = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다.");
        }

        return jwtTokenProvider.createToken(user.getId(), user.getEmail());
    }
}