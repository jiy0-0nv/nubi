package com.nubi.domain.account.controller;

import com.nubi.domain.account.service.AccountService;
import com.nubi.domain.account.dto.*;
import com.nubi.global.exception.UnauthenticatedException;
import com.nubi.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;

/*
## 1. 계정 (Account)


| Method | URI | Request Body | 설명 |
|---|---|---|---|
|oo POST | `/account/login` | `{ "email": string, "password": string }` | 로그인, 성공 시 세션/JWT 쿠키 발급 |
| POST | `/account/logout` | - | 로그아웃, 쿠키 만료 처리 |
|oo POST | `/account/signup` | `{ "email": string, "password": string, "name": string, "phone": string }` | 회원가입 |
|oo POST | `/account/find-id` | `{ "name": string, "phone": string }` | 아이디(이메일) 찾기 |
|oo POST | `/account/find-password` | `{ "email": string, "name": string, "phone": string }` | 비밀번호 재설정 링크/코드 발송 |
| PATCH | `/account/change-password` | `{ "token": string, "new_password": string }` | 발급받은 토큰으로 비밀번호 재설정 |
|oo GET | `/account/{user-id}` | - | 현재 로그인 유저 정보 + role 반환 (기존 `/account/check`, `/account/check/isadmin` 통합) |

 */


@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;
    private final HttpServletRequest request;

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
        Long currentUserId = requireUserId();
        if (!currentUserId.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 계정만 조회할 수 있습니다.");
        }
        AccountResponseDTO response = accountService.getAccount(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/find-password")
    public void findPassword(@RequestBody FindPasswordRequest request){
        accountService.findPassword(request.getEmail());
    }

    @PatchMapping("/change-password")
    public String changePassword(@RequestBody UserTokenRequest request){
        return accountService.changePassword(request.getUserToken(), request.getNewPassword());
    }

    private Long getCurrentUserId() {
        Object userId = request.getAttribute(JwtAuthenticationFilter.USER_ID_ATTRIBUTE);
        return userId instanceof Long ? (Long) userId : null;
    }

    private Long requireUserId() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new UnauthenticatedException();
        }
        return userId;
    }
}
