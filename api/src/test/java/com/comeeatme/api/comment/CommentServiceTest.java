package com.comeeatme.api.comment;

import com.comeeatme.api.comment.request.CommentCreate;
import com.comeeatme.api.comment.request.CommentEdit;
import com.comeeatme.api.comment.response.CommentDto;
import com.comeeatme.api.comment.response.MemberCommentDto;
import com.comeeatme.api.common.response.CreateResult;
import com.comeeatme.api.common.response.DeleteResult;
import com.comeeatme.api.common.response.UpdateResult;
import com.comeeatme.domain.comment.Comment;
import com.comeeatme.domain.comment.CommentEditor;
import com.comeeatme.domain.comment.repository.CommentRepository;
import com.comeeatme.domain.image.Image;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.post.Post;
import com.comeeatme.domain.post.PostImage;
import com.comeeatme.domain.post.repository.PostImageRepository;
import com.comeeatme.domain.post.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

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
    @Mock
    private PostImageRepository postImageRepository;

    @Test
    void create() {
        // given
        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findById(2L)).willReturn(Optional.of(member));

        Post post = mock(Post.class);
        given(post.getUseYn()).willReturn(true);
        given(postRepository.findWithPessimisticLockById(3L)).willReturn(Optional.of(post));

        Comment comment = mock(Comment.class);
        given(comment.getId()).willReturn(1L);

        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
        given(commentRepository.save(commentCaptor.capture())).willReturn(comment);

        CommentCreate commentCreate = CommentCreate.builder()
                .parentId(null)
                .content("test-content")
                .build();

        //when
        CreateResult<Long> result = commentService.create(commentCreate, 2L, 3L);

        // then
        assertThat(result.getId()).isEqualTo(1L);

        Comment capturedComment = commentCaptor.getValue();
        assertThat(capturedComment.getMember()).isEqualTo(member);
        assertThat(capturedComment.getPost()).isEqualTo(post);
        assertThat(capturedComment.getParent()).isNull();
        assertThat(capturedComment.getContent()).isEqualTo("test-content");

        then(post).should().increaseCommentCount();
    }

    @Test
    void edit() {
        // given
        Long commentId = 1L;
        CommentEdit commentEdit = CommentEdit.builder()
                .content("edited-content")
                .build();
        Comment comment = mock(Comment.class);
        given(comment.getUseYn()).willReturn(true);
        given(comment.getId()).willReturn(commentId);
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        CommentEditor.CommentEditorBuilder editorBuilder = mock(CommentEditor.CommentEditorBuilder.class);
        given(comment.toEditor()).willReturn(editorBuilder);
        given(editorBuilder.content("edited-content")).willReturn(editorBuilder);
        CommentEditor editor = mock(CommentEditor.class);
        given(editorBuilder.build()).willReturn(editor);

        // when
        UpdateResult<Long> updateResult = commentService.edit(commentEdit, commentId);

        // then
        then(comment).should().edit(editor);
        assertThat(updateResult.getId()).isEqualTo(commentId);
    }

    @Test
    void isNotOwnedByMember_False() {
        // given
        Member member = mock(Member.class);
        given(memberRepository.getReferenceById(2L)).willReturn(member);
        given(commentRepository.existsByIdAndMember(1L, member)).willReturn(true);

        // expected
        assertThat(commentService.isNotOwnedByMember(1L, 2L)).isFalse();
    }

    @Test
    void isNotOwnedByMember_True() {
        // given
        Member member = mock(Member.class);
        given(memberRepository.getReferenceById(2L)).willReturn(member);
        given(commentRepository.existsByIdAndMember(1L, member)).willReturn(false);

        // expected
        assertThat(commentService.isNotOwnedByMember(1L, 2L)).isTrue();
    }

    @Test
    void isNotBelongToPost() {
        // given
        Post post = mock(Post.class);
        given(postRepository.getReferenceById(2L)).willReturn(post);
        given(commentRepository.existsByIdAndPostAndUseYnIsTrue(1L, post)).willReturn(true);

        // expected
        assertThat(commentService.isNotBelongToPost(1L, 2L)).isFalse();
    }

    @Test
    void delete() {
        // given
        Post post = mock(Post.class);
        given(post.getId()).willReturn(2L);
        Comment comment = mock(Comment.class);
        given(comment.getUseYn()).willReturn(true);
        given(comment.getPost()).willReturn(post);
        given(commentRepository.findById(1L)).willReturn(Optional.of(comment));

        Post lockedPost = mock(Post.class);
        given(lockedPost.getUseYn()).willReturn(true);
        given(postRepository.findWithPessimisticLockById(post.getId())).willReturn(Optional.of(lockedPost));

        // when
        DeleteResult<Long> deleteResult = commentService.delete(1L);

        // then
        assertThat(deleteResult.getId()).isEqualTo(1L);

        then(comment).should().delete();
        then(lockedPost).should().decreaseCommentCount();
    }

    @Test
    void  getListOfPost() {
        // given
        Post post = mock(Post.class);
        given(post.getUseYn()).willReturn(true);
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        Comment parent1 = mock(Comment.class);
        given(parent1.getId()).willReturn(2L);
        Image image1 = mock(Image.class);
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

    @Test
    void getListOfMember() {
        // given
        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        Post post = mock(Post.class);
        given(post.getId()).willReturn(30L);
        given(post.getContent()).willReturn("post-content");
        Comment comment = mock(Comment.class);
        given(comment.getId()).willReturn(10L);
        given(comment.getContent()).willReturn("comment-content");
        given(comment.getCreatedAt()).willReturn(LocalDateTime.of(2022, 12, 19, 23, 18));
        given(comment.getPost()).willReturn(post);
        given(commentRepository.findSliceWithPostByMemberAndUseYnIsTrue(any(Pageable.class), eq(member)))
                .willReturn(new SliceImpl<>(List.of(comment)));

        Image image1 = mock(Image.class);
        given(image1.getUseYn()).willReturn(false); // deleted
        PostImage postImage1 = mock(PostImage.class);
        given(postImage1.getImage()).willReturn(image1);
        Image image2 = mock(Image.class);
        given(image2.getUseYn()).willReturn(true);
        given(image2.getUrl()).willReturn("image-url-2");
        PostImage postImage2 = mock(PostImage.class);
        given(postImage2.getImage()).willReturn(image2);
        given(postImage2.getPost()).willReturn(post);
        Image image3 = mock(Image.class);
        given(image3.getUseYn()).willReturn(true);
        PostImage postImage3 = mock(PostImage.class);
        given(postImage3.getImage()).willReturn(image3);
        given(postImage3.getPost()).willReturn(post);
        given(postImageRepository.findAllWithImageByPostIn(List.of(post)))
                .willReturn(List.of(postImage1, postImage2, postImage3));

        // when
        PageRequest pageRequest = PageRequest.of(0, 10);
        Slice<MemberCommentDto> result = commentService.getListOfMember(pageRequest, 1L);

        // then
        MemberCommentDto memberCommentDto = result.getContent().get(0);
        assertThat(memberCommentDto.getId()).isEqualTo(10L);
        assertThat(memberCommentDto.getContent()).isEqualTo("comment-content");
        assertThat(memberCommentDto.getCreatedAt()).isEqualTo(LocalDateTime.of(2022, 12, 19, 23, 18));
        assertThat(memberCommentDto.getPost().getId()).isEqualTo(30L);
        assertThat(memberCommentDto.getPost().getContent()).isEqualTo("post-content");
        assertThat(memberCommentDto.getPost().getImageUrl()).isEqualTo("image-url-2");
    }

    @Test
    void deleteAllOfMember() {
        // given
        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        Post post1 = mock(Post.class);
        given(post1.getId()).willReturn(10L);
        Post post2 = mock(Post.class);
        given(post2.getId()).willReturn(11L);

        Comment comment1 = mock(Comment.class);
        given(comment1.getPost()).willReturn(post1);
        Comment comment2 = mock(Comment.class);
        given(comment2.getPost()).willReturn(post1);
        Comment comment3 = mock(Comment.class);
        given(comment3.getPost()).willReturn(post2);
        List<Comment> comments = List.of(comment1, comment2, comment3);
        given(commentRepository.findAllByMemberAndUseYnIsTrue(member))
                .willReturn(comments);

        Post lockedPost1 = mock(Post.class);
        given(lockedPost1.getId()).willReturn(10L);
        Post lockedPost2 = mock(Post.class);
        given(lockedPost2.getId()).willReturn(11L);
        given(postRepository.findAllWithPessimisticLockByIdIn(Set.of(10L, 11L)))
                .willReturn(List.of(lockedPost1, lockedPost2));

        // when
        commentService.deleteAllOfMember(1L);

        // then
        comments.forEach(comment -> then(comment).should().delete());
        then(lockedPost1).should(times(2)).decreaseCommentCount();
        then(lockedPost2).should(times(1)).decreaseCommentCount();
    }

}