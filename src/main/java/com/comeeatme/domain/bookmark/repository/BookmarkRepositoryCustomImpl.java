package com.comeeatme.domain.bookmark.repository;

import com.comeeatme.domain.bookmark.Bookmark;
import com.comeeatme.domain.post.Post;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.comeeatme.domain.bookmark.QBookmark.bookmark;

@RequiredArgsConstructor
public class BookmarkRepositoryCustomImpl implements BookmarkRepositoryCustom {

    private final JPAQueryFactory query;

    @Override
    public List<Bookmark> findByMemberIdAndPostIds(Long memberId, List<Long> postIds) {
        return query
                .selectFrom(bookmark)
                .where(
                        bookmark.member.id.eq(memberId),
                        bookmark.post.id.in(postIds)
                ).fetch();
    }

    @Override
    public void deleteAllByPost(Post post) {
        query
                .delete(bookmark)
                .where(bookmark.post.eq(post))
                .execute();
    }

}
