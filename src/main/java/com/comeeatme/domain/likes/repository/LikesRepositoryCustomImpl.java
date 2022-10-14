package com.comeeatme.domain.likes.repository;

import com.comeeatme.domain.likes.response.LikeCount;
import com.comeeatme.domain.likes.response.QLikeCount;
import com.comeeatme.domain.post.Post;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.comeeatme.domain.likes.QLikes.likes;

@RequiredArgsConstructor
public class LikesRepositoryCustomImpl implements LikesRepositoryCustom {

    private final JPAQueryFactory query;

    @Override
    public List<LikeCount> countsGroupByPosts(List<Post> posts) {
        return query
                .select(new QLikeCount(likes.post.id, likes.id.count()))
                .from(likes)
                .where(
                        likes.post.in(posts))
                .groupBy(likes.post)
                .fetch();
    }
}
