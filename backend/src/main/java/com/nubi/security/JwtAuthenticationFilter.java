package com.nubi.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String USER_ID_ATTRIBUTE = "userId";

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(AUTHORIZATION_HEADER);

        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length());
            try {
                Long userId = jwtTokenProvider.getUserIdFromToken(token);
                request.setAttribute(USER_ID_ATTRIBUTE, userId);
            } catch (IllegalArgumentException e) {
                // 토큰이 유효하지 않으면 인증 안 된 상태로 그대로 진행시키고,
                // 각 컨트롤러의 requireUserId()에서 401로 처리한다.
            }
        }

        filterChain.doFilter(request, response);
    }
}
