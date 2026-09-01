package com.nubi.domain.bookmarks;

import com.nubi.entity.BookmarksEntity;
import com.nubi.entity.BookmarksId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookmarksRepository extends JpaRepository<BookmarksEntity, BookmarksId>{

    
}
