package com.comeeatme.domain.bookmark.repository;

import com.comeeatme.domain.bookmark.Bookmark;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.post.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long>, BookmarkRepositoryCustom {

    Optional<Bookmark> findByPostAndMember(Post post, Member member);

    int countByMember(Member member);

    boolean existsByPostAndMember(Post post, Member member);

    @EntityGraph(attributePaths = {"post", "post.restaurant", "post.member"})
    Slice<Bookmark> findSliceWithByMember(Pageable pageable, Member member);

    List<Bookmark> findAllByMember(Member member);

}
