package com.nubi.domain.bookings.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReviewCreateRequestDTO {
    private int rating;
    private String content;
}
