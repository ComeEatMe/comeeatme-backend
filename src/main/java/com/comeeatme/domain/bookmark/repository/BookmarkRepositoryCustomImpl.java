package com.comeeatme.domain.bookmark.repository;

import com.comeeatme.domain.post.Post;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import static com.comeeatme.domain.bookmark.QBookmark.bookmark;

@RequiredArgsConstructor
public class BookmarkRepositoryCustomImpl implements BookmarkRepositoryCustom {

    private final JPAQueryFactory query;

    @Override
    public void deleteAllByPost(Post post) {
        query
                .delete(bookmark)
                .where(bookmark.post.eq(post))
                .execute();
    }

}
