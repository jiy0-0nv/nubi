package com.nubi.domain.account.controller;

import com.nubi.domain.account.service.AccountService;
import com.nubi.domain.account.dto.*;
import com.nubi.entity.UsersEntity;
import com.nubi.global.exception.UnauthenticatedException;
import com.nubi.security.JwtAuthenticationFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;

@Tag(name = "01. 계정 (Account)", description = "회원가입, 로그인, 아이디/비밀번호 찾기, 내 정보 조회 및 탈퇴")
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;
    private final HttpServletRequest request;

    @Operation(
            summary = "회원가입",
            description = """
                    이메일/비밀번호/이름/전화번호로 새 계정을 만듭니다. 인증 토큰이 필요 없습니다.

                    - 비밀번호는 BCrypt 로 해싱되어 저장됩니다.
                    - 성공 시 `Location: /api/accounts/{userId}` 헤더가 함께 내려갑니다.
                    - **응답 본문은 JSON 객체가 아니라 생성된 userId 숫자 하나**입니다.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "가입 성공, 본문은 생성된 userId",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "integer", format = "int64", example = "23"))),
            @ApiResponse(responseCode = "400", description = "이메일 중복 또는 필수값 누락", content = @Content)
    })
    @PostMapping("/signup")
    public ResponseEntity<Long> signup(@RequestBody SignupRequest request) {
        Long userId = accountService.signup(request);
        return ResponseEntity.created(URI.create("/api/accounts/" + userId)).body(userId);
    }

    @Operation(
            summary = "로그인 (JWT 발급)",
            description = """
                    이메일/비밀번호로 로그인합니다.

                    **응답 본문이 JWT 토큰 문자열 그 자체입니다.** (`{"token": "..."}` 형태가 아님)
                    이 문자열을 그대로 복사해 우측 상단 **Authorize** 에 붙여넣으면
                    이후 자물쇠가 붙은 API 를 바로 테스트할 수 있습니다.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공, 본문은 JWT 토큰 문자열",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "string", example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIyMyJ9.xxxxx"))),
            @ApiResponse(responseCode = "400", description = "이메일 또는 비밀번호 불일치", content = @Content)
    })
    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request){
        String userToken = accountService.login(request);
        return userToken;
    }

    @Operation(
            summary = "아이디(이메일) 찾기",
            description = "이름과 전화번호가 모두 일치하는 계정의 이메일을 반환합니다. 응답 본문은 이메일 문자열입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공, 본문은 이메일 문자열",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "string", example = "test2@example.com"))),
            @ApiResponse(responseCode = "400", description = "일치하는 계정 없음", content = @Content)
    })
    @PostMapping("/find-id")
    public String findId(@RequestBody FindIdRequest request){
        String userId = accountService.findId(request);
        return userId;
    }

    @Operation(
            summary = "내 계정 정보 조회",
            description = "토큰의 주인과 `userId` 가 같을 때만 조회할 수 있습니다. 남의 계정을 조회하면 403 입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자", content = @Content)
    })
    @GetMapping("/{userId}")
    public ResponseEntity<AccountResponseDTO> getAccount(
            @Parameter(description = "조회할 사용자 ID (본인 ID 여야 함)", example = "23")
            @PathVariable Long userId) {
        Long currentUserId = requireUserId();
        if (!currentUserId.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 계정만 조회할 수 있습니다.");
        }
        AccountResponseDTO response = accountService.getAccount(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "비밀번호 찾기 (재설정 메일 발송)",
            description = """
                    입력한 이메일로 비밀번호 재설정용 토큰을 메일 발송합니다.
                    받은 토큰은 `PATCH /api/accounts/change-password` 의 `userToken` 에 사용합니다.

                    응답 본문은 없습니다(200 + 빈 body).
                    """)
    @ApiResponse(responseCode = "200", description = "메일 발송 요청 완료", content = @Content)
    @PostMapping("/find-password")
    public void findPassword(@RequestBody FindPasswordRequest request){
        accountService.findPassword(request.getEmail());
    }

    @Operation(
            summary = "비밀번호 재설정",
            description = """
                    메일로 받은 토큰과 새 비밀번호를 함께 보냅니다.

                    이 API 는 **Authorization 헤더를 쓰지 않고 본문의 `userToken` 을 사용**하므로
                    로그인하지 않은 상태에서도 호출할 수 있습니다.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변경 완료 메시지",
                    content = @Content(mediaType = "application/json", schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "400", description = "토큰이 만료되었거나 유효하지 않음", content = @Content)
    })
    @PatchMapping("/change-password")
    public String changePassword(@RequestBody UserTokenRequest request){
        return accountService.changePassword(request.getUserToken(), request.getNewPassword());
    }

    @Operation(
            summary = "회원 탈퇴",
            description = "본인 계정만 탈퇴할 수 있습니다. row 를 지우지 않고 `deleted_at` 을 설정하는 소프트 삭제입니다.")
    @ApiResponse(responseCode = "200", description = "탈퇴 처리 완료", content = @Content)
    @DeleteMapping("/{userId}")
    public void withdraw(
            @Parameter(description = "탈퇴할 사용자 ID (본인 ID 여야 함)", example = "23")
            @PathVariable Long userId) {
        Long currentUserId = requireUserId();
        if (!currentUserId.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 계정만 탈퇴할 수 있습니다.");
        }
        accountService.withdraw(userId);
    }

    // 1. 유저 -> 관리자 권한 부여
    // 지금은 로그인만 했으면 누구나 아무 계정의 role이나 바꿀 수 있음 (의도적으로 열어둔 상태 —
    // ADMIN이 아직 한 명도 없어서 부트스트랩 목적. 실서비스 전엔 반드시 호출 권한을 제한해야 함).
    @Operation(
            summary = "권한(role) 변경 — ⚠️ 부트스트랩 전용",
            description = """
                    계정의 role 을 `USER` 또는 `ADMIN` 으로 변경합니다.

                    ⚠️ **현재는 로그인만 되어 있으면 누구나 아무 계정의 role 을 바꿀 수 있습니다.**
                    ADMIN 계정이 아직 하나도 없어 최초 관리자를 만들기 위해 의도적으로 열어둔 상태이며,
                    실서비스 배포 전에는 반드시 호출 권한을 제한해야 합니다.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변경 완료", content = @Content),
            @ApiResponse(responseCode = "400", description = "role 값이 USER / ADMIN 이 아님", content = @Content)
    })
    @PatchMapping("/{userId}/role")
    public void updateRole(
            @Parameter(description = "권한을 변경할 사용자 ID", example = "23")
            @PathVariable Long userId,
            @RequestBody AccountRoleUpdateRequest request) {
        requireUserId();
        UsersEntity.Role role;
        try {
            role = UsersEntity.Role.valueOf(request.getRole());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "role must be USER or ADMIN");
        }
        accountService.updateRole(userId, role);
    }

    // 2. 관리자 여부 체크 (본인 계정만)
    @Operation(
            summary = "관리자 여부 확인",
            description = "본인 계정이 ADMIN 인지 확인합니다. 프론트에서 관리자 메뉴 노출 여부를 정할 때 사용합니다.")
    @ApiResponse(responseCode = "200", description = "확인 성공")
    @GetMapping("/{userId}/is-admin")
    public AccountAdminStatusResponse isAdmin(
            @Parameter(description = "확인할 사용자 ID (본인 ID 여야 함)", example = "23")
            @PathVariable Long userId) {
        Long currentUserId = requireUserId();
        if (!currentUserId.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 계정만 조회할 수 있습니다.");
        }
        return new AccountAdminStatusResponse(accountService.isAdmin(userId));
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
