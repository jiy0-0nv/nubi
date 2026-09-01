package com.nubi.domain.mypage.service;

import com.nubi.domain.account.dto.AccountResponseDTO;
import com.nubi.domain.account.service.AccountService;
import com.nubi.domain.bookings.service.BookingsService;
import com.nubi.domain.bookmarks.BookmarksRepository;
import com.nubi.domain.bookmarks.BookmarksService;
import com.nubi.domain.mypage.dto.BookingsSummaryDTO;
import com.nubi.domain.mypage.dto.BookmarkSummaryDTO;
import com.nubi.domain.mypage.dto.MypageResponseDTO;
import com.nubi.domain.mypage.dto.ReviewSummaryDTO;
import com.nubi.domain.rooms.RoomsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MypageService {

    private final AccountService accountService;
    private final BookingsService bookingsService;
    private final BookmarksService bookmarksService;
    private final RoomsService roomsService;


    private final BookmarksRepository bookmarksRepository;

    public MypageResponseDTO mypage(long userId){
        AccountResponseDTO profile = accountService.getAccount(userId); // 실제 메서드명에 맞게
        List<BookingsSummaryDTO> bookings = ...;

        List<BookmarkSummaryDTO> bookmarks = bookmarksRepository.findByUserIdWithRoom(userId).stream()
                .map(BookmarkSummaryDTO::from)
                .toList();

        List<ReviewSummaryDTO> reviews = ...;

        return MypageResponseDTO.builder()
                .profile(profile)
                .bookings(bookings)
                .bookmarks(bookmarks)
                .reviews(reviews)
                .build();
    }
}
