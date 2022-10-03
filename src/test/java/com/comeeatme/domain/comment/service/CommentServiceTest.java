package com.comeeatme.domain.comment.service;

import com.comeeatme.domain.comment.Comment;
import com.comeeatme.domain.comment.repository.CommentRepository;
import com.comeeatme.domain.comment.request.CommentCreate;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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
}