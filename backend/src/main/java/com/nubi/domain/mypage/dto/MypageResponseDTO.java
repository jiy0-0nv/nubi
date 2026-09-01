package com.nubi.domain.mypage.dto;

import com.nubi.domain.account.dto.AccountResponseDTO;
import com.nubi.domain.bookings.dto.BookingsResponseDTO;
import com.nubi.domain.bookings.service.BookingsService;
import lombok.Builder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

public class MypageResponseDTO {

    private AccountResponseDTO profile;
    private List<BookingsSummaryDTO> bookings;
    private List<BookmarkSummaryDTO> bookmarks;
    private List<ReviewSummaryDTO> reviews;

    @Builder
    public MypageResponseDTO(AccountResponseDTO profile, List<BookingsSummaryDTO> bookings,
                             List<BookmarkSummaryDTO> bookmarks, List<ReviewSummaryDTO> reviews) {
        this.profile = profile;
        this.bookings = bookings;
        this.bookmarks = bookmarks;
        this.reviews = reviews;
    }
}