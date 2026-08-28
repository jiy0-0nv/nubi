package com.nubi.domain.account;

import com.nubi.domain.account.dto.LoginRequest;
import com.nubi.domain.account.dto.SignupRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/*
## 1. 계정 (Account)


| Method | URI | Request Body | 설명 |
|---|---|---|---|
| POST | `/account/login` | `{ "email": string, "password": string }` | 로그인, 성공 시 세션/JWT 쿠키 발급 |
| POST | `/account/logout` | - | 로그아웃, 쿠키 만료 처리 |
| POST | `/account/signup` | `{ "email": string, "password": string, "name": string, "phone": string }` | 회원가입 |
| POST | `/account/find-id` | `{ "name": string, "phone": string }` | 아이디(이메일) 찾기 |
| POST | `/account/find-password` | `{ "email": string, "name": string, "phone": string }` | 비밀번호 재설정 링크/코드 발송 |
| PATCH | `/account/reset-password` | `{ "token": string, "new_password": string }` | 발급받은 토큰으로 비밀번호 재설정 |
| GET | `/account/me` | - | 현재 로그인 유저 정보 + role 반환 (기존 `/account/check`, `/account/check/isadmin` 통합) |

 */


@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @PostMapping("/signup")
    public ResponseEntity<Long> signup(@RequestBody SignupRequest request) {
        Long userId = accountService.signup(request);
        return ResponseEntity.created(URI.create("/api/accounts/" + userId)).body(userId);
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request){
        String userToken = accountService.login(request);
        return userToken;
    }

}
