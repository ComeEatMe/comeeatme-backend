package com.comeeatme.domain.bookmark.repository;

import com.comeeatme.domain.bookmark.BookmarkGroup;
import com.comeeatme.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookmarkGroupRepository extends JpaRepository<BookmarkGroup, Long> {

    Optional<BookmarkGroup> findByMemberAndName(Member member, String name);

    List<BookmarkGroup> findAllByMember(Member member);

}
