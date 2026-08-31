package com.nubi.domain.account;

import com.nubi.domain.account.dto.AccountResponseDTO;
import com.nubi.domain.account.dto.FindIdRequest;
import com.nubi.domain.account.dto.LoginRequest;
import com.nubi.domain.account.dto.SignupRequest;
import com.nubi.entity.UsersEntity;
import com.nubi.security.JwtTokenProvider;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    //signup() 회원가입
    @Transactional
    public Long signup(SignupRequest request) {
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        UsersEntity user = UsersEntity.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phone(request.getPhone())
                .build();

        return accountRepository.save(user).getId();
    }


    @Transactional(readOnly = true)
    public String login(LoginRequest request) {
        UsersEntity user = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다."));


        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다.");
        }

        return jwtTokenProvider.createToken(user.getId(), user.getEmail());
    }


    @Transactional(readOnly = true)
    public AccountResponseDTO getAccount(Long userId) {
        UsersEntity user = accountRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        return AccountResponseDTO.from(user);
    }


    @Transactional(readOnly = true)
    public String findId(FindIdRequest request){
        UsersEntity user = accountRepository.findByNameAndPhone(request.getName(), request.getPhone())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        return user.getEmail();
    }

    private final JavaMailSender mailSender;

    @Transactional
    public void findPassword(String toEmail) {
        UsersEntity user = accountRepository.findByEmail(toEmail)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        int code = new SecureRandom().nextInt(900000) + 100000;
        String newPassword = String.valueOf(code);

        user.changePassword(passwordEncoder.encode(newPassword));

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("[Nubi] 비밀번호 재설정 안내");
        message.setText(
                "아래의 비밀번호로 로그인해주세요"
                + newPassword + "\n비밀번호를 꼭 갱신해주세요."
        );
        mailSender.send(message);
        accountRepository.save(user);
    }

    @Transactional
    public String changePassword(String token, String newPassword){

        UsersEntity user = accountRepository.findById(jwtTokenProvider.getUserIdFromToken(token))
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        user.changePassword(passwordEncoder.encode(newPassword));

        return "비밀번호 갱신 완료";
    }
}