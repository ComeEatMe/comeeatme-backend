package com.comeeatme.domain.comment.service;

import com.comeeatme.domain.comment.Comment;
import com.comeeatme.domain.comment.repository.CommentRepository;
import com.comeeatme.domain.comment.request.CommentCreate;
import com.comeeatme.domain.comment.request.CommentEdit;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.post.Post;
import com.comeeatme.domain.post.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PostRepository postRepository;

    @Test
    void create() {
        // given
        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findByUsername(anyString())).willReturn(Optional.of(member));

        Post post = mock(Post.class);
        given(post.getUseYn()).willReturn(true);
        given(postRepository.findById(anyLong())).willReturn(Optional.of(post));

        Comment comment = mock(Comment.class);
        given(comment.getId()).willReturn(1L);

        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
        given(commentRepository.save(commentCaptor.capture())).willReturn(comment);

        CommentCreate commentCreate = CommentCreate.builder()
                .parentId(null)
                .content("test-content")
                .build();

        //when
        commentService.create(commentCreate, "test-username", 2L);

        // then
        Comment capturedComment = commentCaptor.getValue();
        assertThat(capturedComment.getMember()).isEqualTo(member);
        assertThat(capturedComment.getPost()).isEqualTo(post);
        assertThat(capturedComment.getParent()).isNull();
        assertThat(capturedComment.getContent()).isEqualTo("test-content");
    }

    @Test
    void edit() {
        // given
        Long commentId = 1L;
        CommentEdit commentEdit = CommentEdit.builder()
                .content("edited-content")
                .build();
        Comment comment = Comment.builder()
                .id(commentId)
                .member(Member.builder().id(2L).build())
                .post(Post.builder().id(3L).build())
                .content("test-content")
                .build();
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // when
        Long editedCommentId = commentService.edit(commentEdit, commentId);

        // then
        assertThat(comment.getContent()).isEqualTo("edited-content");
        assertThat(editedCommentId).isEqualTo(commentId);
    }

    @Test
    void isNotOwnedByMember() {
        // given
        given(commentRepository.existsByIdAndUsernameAndUseYnIsTrue(1L, "test-username")).willReturn(true);

        // expected
        assertThat(commentService.isNotOwnedByMember(1L, "test-username")).isFalse();
    }

    @Test
    void isNotBelongToPost() {
        // given
        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        given(commentRepository.existsByIdAndPostAndUseYnIsTrue(eq(1L), postCaptor.capture())).willReturn(true);

        // expected
        assertThat(commentService.isNotBelongToPost(1L, 2L)).isFalse();
        Post postCaptorValue = postCaptor.getValue();
        assertThat(postCaptorValue.getId()).isEqualTo(2L);
    }

    @Test
    void delete() {
        // given
        Comment comment = Comment.builder()
                .id(1L)
                .build();
        given(commentRepository.findById(1L)).willReturn(Optional.of(comment));

        // when
        Long deletedId = commentService.delete(1L);

        // then
        assertThat(deletedId).isEqualTo(1L);
        assertThat(comment.getUseYn()).isFalse();
    }
}