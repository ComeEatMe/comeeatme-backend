package com.comeeatme.domain.member.repository;

import com.comeeatme.domain.member.Member;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import static com.comeeatme.domain.account.QAccount.account;
import static com.comeeatme.domain.member.QMember.member;

@RequiredArgsConstructor
public class MemberRepositoryCustomImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory query;

    @Override
    public Optional<Member> findByUsername(String username) {
        return Optional.ofNullable(query
                .select(member)
                .from(account)
                .join(account.member, member)
                .where(account.username.eq(username), account.useYn.isTrue())
                .fetchOne()
        );
    }
}
