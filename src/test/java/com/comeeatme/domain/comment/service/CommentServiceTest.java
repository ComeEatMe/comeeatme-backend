package com.comeeatme.domain.comment.service;

import com.comeeatme.domain.comment.Comment;
import com.comeeatme.domain.comment.repository.CommentRepository;
import com.comeeatme.domain.comment.request.CommentCreate;
import com.comeeatme.domain.comment.request.CommentEdit;
import com.comeeatme.domain.comment.response.CommentDto;
import com.comeeatme.domain.common.response.CreateResult;
import com.comeeatme.domain.common.response.UpdateResult;
import com.comeeatme.domain.images.Images;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.time.LocalDateTime;
import java.util.List;
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
        CreateResult<Long> result = commentService.create(commentCreate, "test-username", 2L);

        // then
        assertThat(result.getId()).isEqualTo(1L);

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
        UpdateResult<Long> updateResult = commentService.edit(commentEdit, commentId);

        // then
        assertThat(comment.getContent()).isEqualTo("edited-content");
        assertThat(updateResult.getId()).isEqualTo(commentId);
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

    @Test
    void  getListOfPost() {
        // given
        Post post = mock(Post.class);
        given(post.getUseYn()).willReturn(true);
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        Comment parent1 = mock(Comment.class);
        given(parent1.getId()).willReturn(2L);
        Images image1 = mock(Images.class);
        given(image1.getUrl()).willReturn("image-url-1");
        Member member1 = mock(Member.class);
        given(member1.getId()).willReturn(3L);
        given(member1.getNickname()).willReturn("nickname-1");
        given(member1.getImage()).willReturn(image1);
        Comment comment1 = mock(Comment.class);
        given(comment1.getId()).willReturn(4L);
        given(comment1.getParent()).willReturn(parent1);
        given(comment1.getContent()).willReturn("content-1");
        given(comment1.getCreatedAt()).willReturn(LocalDateTime.of(2022, 3, 1, 12, 30));
        given(comment1.getMember()).willReturn(member1);
        given(comment1.getUseYn()).willReturn(true);

        Member member2 = mock(Member.class);
        given(member2.getId()).willReturn(5L);
        given(member2.getNickname()).willReturn("nickname-2");
        given(member2.getImage()).willReturn(null);
        Comment comment2 = mock(Comment.class);
        given(comment2.getId()).willReturn(6L);
        given(comment2.getParent()).willReturn(null);
        given(comment2.getContent()).willReturn("content-2");
        given(comment2.getCreatedAt()).willReturn(LocalDateTime.of(2022, 4, 1, 13, 30));
        given(comment2.getMember()).willReturn(member2);
        given(comment2.getUseYn()).willReturn(true);

        Comment parent3 = mock(Comment.class);
        given(parent3.getId()).willReturn(7L);
        Comment comment3 = mock(Comment.class);
        given(comment3.getId()).willReturn(9L);
        given(comment3.getParent()).willReturn(parent3);

        SliceImpl<Comment> commentSlice = new SliceImpl<>(List.of(comment1, comment2, comment3));
        given(commentRepository.findSliceByPostWithMemberAndImage(any(PageRequest.class), eq(post)))
                .willReturn(commentSlice);

        // when
        Slice<CommentDto> commentDtoSlice = commentService.getListOfPost(PageRequest.of(0, 10), 1L);

        // then

        List<CommentDto> content = commentDtoSlice.getContent();
        assertThat(content).hasSize(3);
        assertThat(content).extracting("id").containsExactly(4L, 6L, 9L);
        assertThat(content).extracting("parentId").containsExactly(2L, null, 7L);
        assertThat(content).extracting("createdAt").containsExactly(
                LocalDateTime.of(2022, 3, 1, 12, 30), LocalDateTime.of(2022, 4, 1, 13, 30), null);
        assertThat(content).extracting("member").extracting("id").containsExactly(3L, 5L, null);
        assertThat(content).extracting("member").extracting("nickname")
                .containsExactly("nickname-1", "nickname-2", null);
        assertThat(content).extracting("member").extracting("imageUrl").containsExactly("image-url-1", null, null);
    }
}