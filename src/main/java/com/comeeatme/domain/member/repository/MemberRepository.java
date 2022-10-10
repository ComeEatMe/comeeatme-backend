package com.comeeatme.domain.member.repository;

import com.comeeatme.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

    boolean existsByNickname(String nickname);
}
