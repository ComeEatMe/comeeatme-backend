package com.comeeatme.domain.post.repository;

import com.comeeatme.domain.post.Hashtag;
import com.comeeatme.domain.post.Post;
import com.comeeatme.domain.post.request.PostSearch;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;
import java.util.Optional;

import static com.comeeatme.domain.member.QMember.member;
import static com.comeeatme.domain.post.QPost.post;
import static com.comeeatme.domain.post.QPostHashtag.postHashtag;
import static com.comeeatme.domain.restaurant.QRestaurant.restaurant;

@RequiredArgsConstructor
public class PostRepositoryCustomImpl implements PostRepositoryCustom {

    private final JPAQueryFactory query;

    @Override
    public Slice<Post> findSliceWithMemberAndRestaurantBy(Pageable pageable, PostSearch postSearch) {
        JPAQuery<Post> contentQuery = query
                .selectFrom(post)
                .join(post.member, member).fetchJoin()
                .join(post.restaurant, restaurant).fetchJoin();
        Optional.ofNullable(postSearch.getHashtags()).ifPresent(hashtags ->
                hashtags.forEach(hashtag -> contentQuery.where(postIdOf(hashtag))));
        contentQuery.where(post.useYn.isTrue());
        List<Post> content = contentQuery
                .orderBy(post.id.desc())
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

    private BooleanExpression postIdOf(Hashtag hashtag) {
        return Optional.ofNullable(hashtag)
                .map(h -> post.id.in(JPAExpressions
                        .select(postHashtag.post.id)
                        .from(postHashtag)
                        .where(postHashtag.hashtag.eq(h))
                ))
                .orElse(null);
    }

}
