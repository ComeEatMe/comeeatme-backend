package com.comeeatme.domain.comment.repository;

import com.querydsl.core.types.Expression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import static com.comeeatme.domain.account.QAccount.account;
import static com.comeeatme.domain.comment.QComment.comment;
import static com.comeeatme.domain.member.QMember.member;

@RequiredArgsConstructor
public class CommentRepositoryCustomImpl implements CommentRepositoryCustom {

    private final JPAQueryFactory query;

    @Override
    public boolean existsByIdAndUsernameAndUseYnIsTrue(Long commentId, String username) {
        return Optional.ofNullable(query
                        .selectOne()
                        .from(comment)
                        .join(comment.member, member)
                        .where(
                                comment.id.eq(commentId),
                                comment.useYn.isTrue(),
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
