package com.nubi.domain.bookingsoo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BookingCancelRequestDTO {
    private String reason; // 받기는 하지만 저장 컬럼이 없어 현재는 사용하지 않음
}
