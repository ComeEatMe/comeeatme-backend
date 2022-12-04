package com.comeeatme.domain.comment.service;

import com.comeeatme.domain.comment.Comment;
import com.comeeatme.domain.comment.CommentEditor;
import com.comeeatme.domain.comment.repository.CommentRepository;
import com.comeeatme.domain.comment.request.CommentCreate;
import com.comeeatme.domain.comment.request.CommentEdit;
import com.comeeatme.domain.comment.response.CommentDto;
import com.comeeatme.domain.common.response.CreateResult;
import com.comeeatme.domain.common.response.DeleteResult;
import com.comeeatme.domain.common.response.UpdateResult;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.post.Post;
import com.comeeatme.domain.post.repository.PostRepository;
import com.comeeatme.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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
    public CreateResult<Long> create(CommentCreate commentCreate, Long memberId, Long postId) {
        Member member = getMemberById(memberId);
        Post post = getPostWithPessimisticLockById(postId);
        Comment parent = Optional.ofNullable(commentCreate.getParentId())
                .map(this::getCommentById)
                .orElse(null);
        Comment comment = commentRepository.save(Comment.builder()
                        .member(member)
                        .post(post)
                        .parent(parent)
                        .content(commentCreate.getContent())
                .build());
        post.increaseCommentCount();
        return new CreateResult<>(comment.getId());
    }

    @Transactional
    public UpdateResult<Long> edit(CommentEdit commentEdit, Long commentId) {
        Comment comment = getCommentById(commentId);
        CommentEditor editor = comment.toEditor()
                .content(commentEdit.getContent())
                .build();
        comment.edit(editor);
        return new UpdateResult<>(comment.getId());
    }

    public boolean isNotOwnedByMember(Long commentId, String username) {
        return !commentRepository.existsByIdAndUsernameAndUseYnIsTrue(commentId, username);
    }

    public boolean isNotBelongToPost(Long commentId, Long postId) {
        return !commentRepository.existsByIdAndPostAndUseYnIsTrue(commentId, Post.builder().id(postId).build());
    }

    @Transactional
    public DeleteResult<Long> delete(Long commentId) {
        Comment comment = getCommentById(commentId);
        comment.delete();
        Post post = getPostWithPessimisticLockById(comment.getPost().getId());
        post.decreaseCommentCount();
        return new DeleteResult<>(commentId);
    }

    public Slice<CommentDto> getListOfPost(Pageable pageable, Long postId) {
        Post post = getPostById(postId);
        return commentRepository.findSliceByPostWithMemberAndImage(pageable, post)
                .map(CommentDto::of);
    }

    private Member getMemberById(Long id) {
        return memberRepository.findById(id)
                .filter(Member::getUseYn)
                .orElseThrow(() -> new EntityNotFoundException("Member.id=" + id));
    }

    private Post getPostById(Long postId) {
        return postRepository.findById(postId)
                .filter(Post::getUseYn)
                .orElseThrow(() -> new EntityNotFoundException("Post id=" + postId));
    }

    private Post getPostWithPessimisticLockById(Long postId) {
        return postRepository.findWithPessimisticLockById(postId)
                .filter(Post::getUseYn)
                .orElseThrow(() -> new EntityNotFoundException("Post id=" + postId));
    }

    private Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .filter(Comment::getUseYn)
                .orElseThrow(() -> new EntityNotFoundException("Comment id=" + commentId));
    }
}
