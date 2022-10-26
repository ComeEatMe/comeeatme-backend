package com.comeeatme.domain.likes.repository;

import com.comeeatme.domain.likes.Likes;
import com.comeeatme.domain.likes.QLikes;
import com.comeeatme.domain.likes.response.LikeCount;
import com.comeeatme.domain.likes.response.LikedResult;
import com.comeeatme.domain.likes.response.QLikeCount;
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
import static com.comeeatme.domain.likes.QLikes.likes;
import static com.comeeatme.domain.member.QMember.member;

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

    @Override
    public List<LikedResult> existsByPostIdsAndUsername(List<Long> postIds, String username) {
        List<Likes> likes = query
                .selectFrom(QLikes.likes)
                .where(
                        QLikes.likes.post.id.in(postIds),
                        QLikes.likes.member.eq(memberOfUsername(username))
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
        List<Likes> likes = query
                .selectFrom(QLikes.likes)
                .where(
                        QLikes.likes.post.id.in(postIds),
                        QLikes.likes.member.id.eq(memberId)
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
