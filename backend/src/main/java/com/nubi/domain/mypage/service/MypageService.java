package com.nubi.domain.mypage.service;

import com.nubi.domain.account.service.AccountService;
import com.nubi.domain.bookings.service.BookingsService;
import com.nubi.domain.bookmarks.BookmarksService;
import com.nubi.domain.mypage.dto.MypageResponseDTO;
import com.nubi.domain.rooms.RoomsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MypageService {

    private final AccountService accountService;
    private final BookingsService bookingsService;
    private final BookmarksService bookmarksService;
    private final RoomsService roomsService;

    public MypageResponseDTO mypage(long userId){

    }
}
