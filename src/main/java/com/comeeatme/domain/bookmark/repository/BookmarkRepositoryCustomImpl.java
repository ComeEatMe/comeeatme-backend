package com.comeeatme.domain.bookmark.repository;

import com.comeeatme.domain.bookmark.Bookmark;
import com.comeeatme.domain.post.Post;
import com.comeeatme.domain.bookmark.BookmarkGroup;
import com.comeeatme.domain.member.Member;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

import static com.comeeatme.domain.bookmark.QBookmark.bookmark;
import static com.comeeatme.domain.post.QPost.post;

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

    public Slice<Bookmark> findSliceWithByMemberAndGroup(
            Pageable pageable, Member member, @Nullable BookmarkGroup group) {
        List<Bookmark> content = query
                .selectFrom(bookmark)
                .join(bookmark.post, post).fetchJoin()
                .join(post.restaurant).fetchJoin()
                .join(bookmark.member).fetchJoin()
                .where(
                        bookmark.member.eq(member),
                        bookmarkGroupEq(group)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1L)
                .fetch();

        boolean hasNext = false;
        if (content.size() > pageable.getPageSize()) {
            content.remove(pageable.getPageSize());
            hasNext = true;
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }

    public BooleanExpression bookmarkGroupEq(@Nullable BookmarkGroup group) {
        return Optional.ofNullable(group)
                .map(bookmark.group::eq)
                .orElse(null);
    }

}
