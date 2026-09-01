package com.nubi.domain.bookmarks;

import com.nubi.entity.BookmarksEntity;
import com.nubi.entity.BookmarksId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookmarksRepository extends JpaRepository<BookmarksEntity, BookmarksId>{

    @Query("SELECT b FROM BookmarksEntity b JOIN FETCH b.room WHERE b.user.id = :userId ORDER BY b.createdAt DESC")
    List<BookmarksEntity> findByUserIdWithRoom(@Param("userId") Long userId);
}
