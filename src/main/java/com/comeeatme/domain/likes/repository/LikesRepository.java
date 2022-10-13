package com.comeeatme.domain.likes.repository;

import com.comeeatme.domain.likes.Likes;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikesRepository extends JpaRepository<Likes, Long> {

    Optional<Likes> findByPostAndMember(Post post, Member member);

    Integer countByPost(Post post);
}
