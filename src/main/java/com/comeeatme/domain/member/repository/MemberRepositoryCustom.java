package com.comeeatme.domain.member.repository;

import com.comeeatme.domain.member.Member;

import java.util.Optional;

public interface MemberRepositoryCustom {

    Optional<Member> findByUsername(String username);
}
