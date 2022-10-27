package com.comeeatme.domain.bookmark.repository;

import com.comeeatme.domain.bookmark.BookmarkGroup;
import com.comeeatme.domain.post.Post;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.util.Optional;

import static com.comeeatme.domain.bookmark.QBookmark.bookmark;

@RequiredArgsConstructor
public class BookmarkRepositoryCustomImpl implements BookmarkRepositoryCustom {

    private final JPAQueryFactory query;

}
