package com.nubi.global.exception;

import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnauthenticatedException.class)
    public ResponseEntity<Map<String, String>> handleUnauthenticated() {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("errorCode", "NEED_SIGNUP"));
    }

    // 같은 방에 락이 몰려 대기 타임아웃이 나는 경우 (동시 예약 시도 충돌)
    @ExceptionHandler(PessimisticLockingFailureException.class)
    public ResponseEntity<Map<String, String>> handleLockConflict() {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("errorCode", "BOOKING_LOCK_CONFLICT"));
    }
}
