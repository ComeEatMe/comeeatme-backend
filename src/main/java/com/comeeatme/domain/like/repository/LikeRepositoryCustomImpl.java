package com.comeeatme.domain.like.repository;

import com.comeeatme.domain.like.Like;
import com.comeeatme.domain.like.QLike;
import com.comeeatme.domain.like.response.LikeCount;
import com.comeeatme.domain.like.response.LikedResult;
import com.comeeatme.domain.like.response.QLikeCount;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.post.Post;
import com.querydsl.core.types.Expression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.comeeatme.domain.account.QAccount.account;
import static com.comeeatme.domain.member.QMember.member;

@RequiredArgsConstructor
public class LikeRepositoryCustomImpl implements LikeRepositoryCustom {

    private final JPAQueryFactory query;

    @Override
    public List<LikeCount> countsGroupByPosts(List<Post> posts) {
        return query
                .select(new QLikeCount(QLike.like.post.id, QLike.like.id.count()))
                .from(QLike.like)
                .where(
                        QLike.like.post.in(posts))
                .groupBy(QLike.like.post)
                .fetch();
    }

    @Override
    public List<LikedResult> existsByPostIdsAndUsername(List<Long> postIds, String username) {
        List<Like> likes = query
                .selectFrom(QLike.like)
                .where(
                        QLike.like.post.id.in(postIds),
                        QLike.like.member.eq(memberOfUsername(username))
                ).fetch();
        Set<Long> existPostIds = likes.stream()
                .map(like -> like.getPost().getId())
                .collect(Collectors.toSet());
        return postIds.stream()
                .map(postId -> LikedResult.builder()
                        .postId(postId)
                        .liked(existPostIds.contains(postId))
                        .build()
                ).collect(Collectors.toList());
    }

    private Expression<Member> memberOfUsername(String username) {
        return JPAExpressions
                .select(member)
                .from(account)
                .join(account.member, member)
                .where(account.username.eq(username));
    }

    @Override
    public List<LikedResult> existsByPostIdsAndMemberId(List<Long> postIds, Long memberId) {
        List<Like> likes = query
                .selectFrom(QLike.like)
                .where(
                        QLike.like.post.id.in(postIds),
                        QLike.like.member.id.eq(memberId)
                ).fetch();
        Set<Long> existPostIds = likes.stream()
                .map(like -> like.getPost().getId())
                .collect(Collectors.toSet());
        return postIds.stream()
                .map(postId -> LikedResult.builder()
                        .postId(postId)
                        .liked(existPostIds.contains(postId))
                        .build()
                ).collect(Collectors.toList());
    }

}
