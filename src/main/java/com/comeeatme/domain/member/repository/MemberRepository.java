package com.comeeatme.domain.member.repository;

import com.comeeatme.domain.member.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByNickname(String nickname);

    @EntityGraph(attributePaths = "image")
    Slice<Member> findSliceWithImagesByNicknameStartingWith(Pageable pageable, String nickname);
}
