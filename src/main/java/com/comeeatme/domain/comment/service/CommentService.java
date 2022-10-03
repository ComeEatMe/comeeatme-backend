package com.comeeatme.domain.comment.service;

import com.comeeatme.domain.comment.Comment;
import com.comeeatme.domain.comment.repository.CommentRepository;
import com.comeeatme.domain.comment.request.CommentCreate;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.post.Post;
import com.comeeatme.domain.post.repository.PostRepository;
import com.comeeatme.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    private final MemberRepository memberRepository;

    private final PostRepository postRepository;

    @Transactional
    public Long create(CommentCreate commentCreate, String username, Long postId) {
        Member member = getMemberByUsername(username);
        Post post = getPostById(postId);
        Comment parent = Optional.ofNullable(commentCreate.getParentId())
                .map(this::getCommentById)
                .orElse(null);
        Comment comment = commentRepository.save(Comment.builder()
                        .member(member)
                        .post(post)
                        .parent(parent)
                        .content(commentCreate.getContent())
                .build());
        return comment.getId();
    }

    private Member getMemberByUsername(String username) {
        return memberRepository.findByUsername(username)
                .filter(Member::getUseYn)
                .orElseThrow(() -> new EntityNotFoundException("Member username=" + username));
    }

    private Post getPostById(Long postId) {
        return postRepository.findById(postId)
                .filter(Post::getUseYn)
                .orElseThrow(() -> new EntityNotFoundException("Post id=" + postId));
    }

    private Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .filter(Comment::getUseYn)
                .orElseThrow(() -> new EntityNotFoundException("Comment id=" + commentId));
    }
}
