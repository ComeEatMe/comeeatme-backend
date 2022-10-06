package com.comeeatme.domain.post.repository;

import com.querydsl.core.types.Expression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import static com.comeeatme.domain.account.QAccount.account;
import static com.comeeatme.domain.member.QMember.member;
import static com.comeeatme.domain.post.QPost.post;

@RequiredArgsConstructor
public class PostRepositoryCustomImpl implements PostRepositoryCustom {

    private final JPAQueryFactory query;

    @Override
    public boolean existsByIdAndUsernameAndUseYnIsTrue(Long postId, String username) {
        return Optional.ofNullable(query
                        .selectOne()
                        .from(post)
                        .join(post.member, member)
                        .where(
                                post.id.eq(postId),
                                post.useYn.isTrue(),
                                member.id.eq(memberIdOfUsername(username)))
                        .fetchOne())
                .isPresent();
    }

    private Expression<Long> memberIdOfUsername(String username) {
        return JPAExpressions
                .select(member.id)
                .from(account)
                .join(account.member, member)
                .where(account.username.eq(username));
    }

}
