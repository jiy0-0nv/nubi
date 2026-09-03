package com.nubi.domain.mypage.controller;

import com.nubi.domain.mypage.dto.MypageResponseDTO;
import com.nubi.domain.mypage.service.MypageService;
import com.nubi.global.exception.UnauthenticatedException;
import com.nubi.security.JwtAuthenticationFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "05. 마이페이지 (Mypage)", description = "내 프로필 · 예약 내역 · 북마크 · 작성 리뷰를 한 번에 조회")
@RestController
@RequiredArgsConstructor
public class MypageController {

    private final MypageService mypageService;
    private final HttpServletRequest request;

    //내 관련 모든 정보 조회 - 북마크 내역, 예약 내역 내 정보 조회
    @Operation(
            summary = "마이페이지 통합 조회",
            description = """
                    토큰 주인의 프로필(`profile`), 예약 내역(`bookings`), 북마크(`bookmarks`),
                    작성한 리뷰(`reviews`) 를 한 번의 호출로 모두 반환합니다.

                    ℹ️ 조회 API 이지만 **HTTP 메서드가 `POST`** 입니다. (요청 본문은 없습니다)
                    """)
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @PostMapping("/api/mypage")
    public MypageResponseDTO mypage(){
        Long userId = requireUserId();
        return mypageService.mypage(userId);
    }

    //Patch는 없어도 되나요? 바꿀만한 내용이 이름, 전화번호?? -> 변경 가능하게 할지..?

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
