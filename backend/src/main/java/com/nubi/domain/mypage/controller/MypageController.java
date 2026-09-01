package com.nubi.domain.mypage.controller;

import com.nubi.domain.mypage.dto.MypageResponseDTO;
import com.nubi.domain.mypage.service.MypageService;
import com.nubi.global.exception.UnauthenticatedException;
import com.nubi.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class MypageController {

    private final MypageService mypageService;
    private final HttpServletRequest request;

    //내 관련 모든 정보 조회 - 북마크 내역, 예약 내역 내 정보 조회
    @PostMapping("/mypage")
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
