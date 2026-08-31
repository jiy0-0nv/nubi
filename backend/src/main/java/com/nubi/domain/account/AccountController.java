package com.nubi.domain.account;

import com.nubi.domain.account.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/*
## 1. 계정 (Account)


| Method | URI | Request Body | 설명 |
|---|---|---|---|
|oo POST | `/account/login` | `{ "email": string, "password": string }` | 로그인, 성공 시 세션/JWT 쿠키 발급 |
| POST | `/account/logout` | - | 로그아웃, 쿠키 만료 처리 |
|oo POST | `/account/signup` | `{ "email": string, "password": string, "name": string, "phone": string }` | 회원가입 |
|oo POST | `/account/find-id` | `{ "name": string, "phone": string }` | 아이디(이메일) 찾기 |
| POST | `/account/find-password` | `{ "email": string, "name": string, "phone": string }` | 비밀번호 재설정 링크/코드 발송 |
| PATCH | `/account/reset-password` | `{ "token": string, "new_password": string }` | 발급받은 토큰으로 비밀번호 재설정 |
|oo GET | `/account/{user-id}` | - | 현재 로그인 유저 정보 + role 반환 (기존 `/account/check`, `/account/check/isadmin` 통합) |

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

    @PostMapping("/find-id")
    public String findId(@RequestBody FindIdRequest request){
        String userId = accountService.findId(request);
        return userId;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<AccountResponseDTO> getAccount(@PathVariable Long userId) {
        AccountResponseDTO response = accountService.getAccount(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/find-password")
    public void findPassword(@RequestBody FindPasswordRequest request){
        accountService.findPassword(request.getEmail());
    }

}
