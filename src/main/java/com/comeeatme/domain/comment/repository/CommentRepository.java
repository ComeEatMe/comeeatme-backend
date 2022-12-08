package com.comeeatme.domain.comment.repository;

import com.comeeatme.domain.comment.Comment;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom {

    boolean existsByIdAndPostAndUseYnIsTrue(Long id, Post post);

    List<Comment> findAllByPostAndUseYnIsTrue(Post post);

    boolean existsByIdAndMember(Long id, Member member);

}
