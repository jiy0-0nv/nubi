package com.nubi.domain.bookmarks;

import com.nubi.entity.BookmarksEntity;
import com.nubi.entity.BookmarksId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository 
public interface BookmarksRepository extends JpaRepository<BookmarksEntity, BookmarksId> {
    Page<BookmarksEntity> findById_UserId(Long userId, Pageable pageable);
}
