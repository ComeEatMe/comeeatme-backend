package com.comeeatme.domain.like.repository;

import com.comeeatme.domain.like.Like;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.post.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long>, LikeRepositoryCustom {

    Optional<Like> findByPostAndMember(Post post, Member member);

    boolean existsByPostAndMember(Post post, Member member);

    @EntityGraph(attributePaths = "post")
    Slice<Like> findSliceWithPostByMember(Pageable pageable, Member member);

}
