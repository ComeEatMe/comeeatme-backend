package com.comeeatme.domain.like.repository;

import com.comeeatme.domain.like.Like;
import com.comeeatme.domain.like.response.LikeCount;
import com.comeeatme.domain.like.response.QLikeCount;
import com.comeeatme.domain.post.Post;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.comeeatme.domain.like.QLike.like;

@RequiredArgsConstructor
public class LikeRepositoryCustomImpl implements LikeRepositoryCustom {

    private final JPAQueryFactory query;

    @Override
    public List<LikeCount> countsGroupByPosts(List<Post> posts) {
        return query
                .select(new QLikeCount(like.post.id, like.id.count()))
                .from(like)
                .where(
                        like.post.in(posts))
                .groupBy(like.post)
                .fetch();
    }

    @Override
    public List<Like> findByMemberIdAndPostIds(Long memberId, List<Long> postIds) {
        return query
                .selectFrom(like)
                .where(
                        like.post.id.in(postIds),
                        like.member.id.eq(memberId)
                ).fetch();
    }

    @Override
    public void deleteAllByPost(Post post) {
        query
                .delete(like)
                .where(like.post.eq(post))
                .execute();
    }

}
