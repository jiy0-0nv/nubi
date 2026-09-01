package com.nubi.domain.mypage.dto;

import com.nubi.domain.account.dto.AccountResponseDTO;
import com.nubi.domain.bookings.dto.ReviewResponseDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class MypageResponseDTO {

    private AccountResponseDTO profile;
    private List<BookingsSummaryDTO> bookings;
    private List<BookmarkSummaryDTO> bookmarks;
    private List<ReviewResponseDTO> reviews;

    @Builder
    public MypageResponseDTO(AccountResponseDTO profile, List<BookingsSummaryDTO> bookings,
                             List<BookmarkSummaryDTO> bookmarks, List<ReviewResponseDTO> reviews) {
        this.profile = profile;
        this.bookings = bookings;
        this.bookmarks = bookmarks;
        this.reviews = reviews;
    }
}