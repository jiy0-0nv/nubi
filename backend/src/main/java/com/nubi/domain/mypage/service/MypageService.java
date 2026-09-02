package com.nubi.domain.mypage.service;

import com.nubi.domain.account.dto.AccountResponseDTO;
import com.nubi.domain.account.service.AccountService;
import com.nubi.domain.bookings.dto.ReviewResponseDTO;
import com.nubi.domain.bookings.repository.ReviewsRepository;
import com.nubi.domain.bookings.service.BookingsService;
import com.nubi.domain.bookmarks.repository.BookmarksRepository;
import com.nubi.domain.mypage.dto.BookingsSummaryDTO;
import com.nubi.domain.mypage.dto.BookmarkSummaryDTO;
import com.nubi.domain.mypage.dto.MypageResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MypageService {

    private final AccountService accountService;
    private final ReviewsRepository reviewsRepository;
    private final BookingsService bookingService;
    private final BookmarksRepository bookmarksRepository;

    public MypageResponseDTO mypage(long userId) {
        AccountResponseDTO profile = accountService.getAccount(userId); // 실제 메서드명에 맞게
        List<BookingsSummaryDTO> bookings = bookingService.getBookings(
                        userId,
                        null,  // 상태 필터 없이 전체
                        PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "checkInDate"))
                )
                .getContent().stream()
                .map(BookingsSummaryDTO::from)
                .toList();


        List<BookmarkSummaryDTO> bookmarks = bookmarksRepository.findByUserIdWithRoom(userId).stream()
                .map(BookmarkSummaryDTO::from)
                .toList();

        List<ReviewResponseDTO> reviews = reviewsRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(ReviewResponseDTO::from)
                .toList();

        return MypageResponseDTO.builder()
                .profile(profile)
                .bookings(bookings)
                .bookmarks(bookmarks)
                .reviews(reviews)
                .build();
    }
}
