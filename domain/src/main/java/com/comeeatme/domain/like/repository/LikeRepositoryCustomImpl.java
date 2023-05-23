package com.comeeatme.domain.like.repository;

import com.comeeatme.domain.post.Post;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import static com.comeeatme.domain.like.QLike.like;

@RequiredArgsConstructor
public class LikeRepositoryCustomImpl implements LikeRepositoryCustom {

    private final JPAQueryFactory query;

    @Override
    public void deleteAllByPost(Post post) {
        query
                .delete(like)
                .where(like.post.eq(post))
                .execute();
    }

}
