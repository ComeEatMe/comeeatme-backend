package com.comeeatme.domain.bookmark.repository;

import com.comeeatme.domain.bookmark.Bookmark;
import com.comeeatme.domain.bookmark.BookmarkGroup;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long>, BookmarkRepositoryCustom {

    boolean existsByGroupAndPost(BookmarkGroup group, Post post);

    Optional<Bookmark> findByGroupAndPost(BookmarkGroup group, Post post);

    int countByMember(Member member);

    boolean existsByMemberAndPost(Member member, Post post);

}
