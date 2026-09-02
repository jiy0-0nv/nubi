package com.nubi.domain.bookmarks.service;

import com.nubi.domain.bookmarks.repository.BookmarksRepository;
import com.nubi.domain.rooms.repository.RoomsRepository;
import com.nubi.domain.account.repository.AccountRepository;
import com.nubi.entity.BookmarksEntity;
import com.nubi.entity.BookmarksId;
import com.nubi.entity.RoomsEntity;
import com.nubi.entity.UsersEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class BookmarksService {

    private final BookmarksRepository bookmarksRepository;
    private final RoomsRepository roomsRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public void addBookmark(Long userId, Long roomId) {
        RoomsEntity room = roomsRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "숙소를 찾을 수 없습니다. id=" + roomId));

        BookmarksId bookmarksId = new BookmarksId(userId, roomId);
        if (bookmarksRepository.existsById(bookmarksId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 북마크한 숙소입니다.");
        }

        UsersEntity userRef = accountRepository.getReferenceById(userId);

        BookmarksEntity bookmark = BookmarksEntity.builder()
                .user(userRef)
                .room(room)
                .build();
        bookmarksRepository.save(bookmark);
    }

    @Transactional
    public void removeBookmark(Long userId, Long roomId) {
        BookmarksId bookmarksId = new BookmarksId(userId, roomId);
        if (!bookmarksRepository.existsById(bookmarksId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "북마크를 찾을 수 없습니다.");
        }
        bookmarksRepository.deleteById(bookmarksId);
    }

    @Transactional(readOnly = true)
    public boolean isBookmarked(Long userId, Long roomId) {
        return bookmarksRepository.existsById(new BookmarksId(userId, roomId));
    }

}