package com.comeeatme.api.like;

import com.comeeatme.api.exception.AlreadyLikedPostException;
import com.comeeatme.api.exception.EntityNotFoundException;
import com.comeeatme.api.like.response.LikedPostDto;
import com.comeeatme.api.like.response.PostLiked;
import com.comeeatme.domain.image.Image;
import com.comeeatme.domain.like.Like;
import com.comeeatme.domain.like.repository.LikeRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @InjectMocks
    private LikeService likeService;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PostImageRepository postImageRepository;

    @Test
    void like() {
        // given
        Post post = mock(Post.class);
        given(post.getUseYn()).willReturn(true);
        given(postRepository.findWithPessimisticLockById(1L)).willReturn(Optional.of(post));

        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findById(2L)).willReturn(Optional.of(member));

        given(likeRepository.existsByPostAndMember(post, member)).willReturn(false);

        // when
        likeService.like(1L, 2L);

        // then
        ArgumentCaptor<Like> likesCaptor = ArgumentCaptor.forClass(Like.class);
        then(likeRepository).should().save(likesCaptor.capture());
        Like likeCaptorValue = likesCaptor.getValue();
        assertThat(likeCaptorValue.getPost()).isEqualTo(post);
        assertThat(likeCaptorValue.getMember()).isEqualTo(member);

        then(post).should().increaseLikeCount();
    }

    @Test
    void like_AlreadyLiked() {
        // given
        Post post = mock(Post.class);
        given(post.getUseYn()).willReturn(true);
        given(post.getId()).willReturn(1L);
        given(postRepository.findWithPessimisticLockById(1L)).willReturn(Optional.of(post));

        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findById(2L)).willReturn(Optional.of(member));

        given(likeRepository.existsByPostAndMember(post, member)).willReturn(true);

        // expected
        assertThatThrownBy(() -> likeService.like(1L, 2L))
                .isInstanceOf(AlreadyLikedPostException.class);
        then(post).should(never()).increaseLikeCount();
    }

    @Test
    void unlike() {
        // given
        Post post = mock(Post.class);
        given(post.getUseYn()).willReturn(true);
        given(postRepository.findWithPessimisticLockById(1L)).willReturn(Optional.of(post));

        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findById(2L)).willReturn(Optional.of(member));

        Like like = mock(Like.class);
        given(likeRepository.findByPostAndMember(post, member)).willReturn(Optional.of(like));

        // when
        likeService.unlike(1L, 2L);

        // then
        then(likeRepository).should().delete(like);
        then(post).should().decreaseLikeCount();
    }

    @Test
    void unlike_NotLiked() {
        // given
        Post post = mock(Post.class);
        given(post.getUseYn()).willReturn(true);
        given(post.getId()).willReturn(1L);
        given(postRepository.findWithPessimisticLockById(1L)).willReturn(Optional.of(post));

        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findById(2L)).willReturn(Optional.of(member));

        given(likeRepository.findByPostAndMember(post, member)).willReturn(Optional.empty());

        // expected
        assertThatThrownBy(() -> likeService.unlike(1L, 2L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void areLiked() {
        // given
        Member member = mock(Member.class);
        given(memberRepository.getReferenceById(3L)).willReturn(member);

        Post refPost1 = mock(Post.class);
        given(postRepository.getReferenceById(1L)).willReturn(refPost1);
        Post refPost2 = mock(Post.class);
        given(postRepository.getReferenceById(2L)).willReturn(refPost2);
        Post refPost3 = mock(Post.class);
        given(postRepository.getReferenceById(3L)).willReturn(refPost3);

        Post post1 = mock(Post.class);
        given(post1.getId()).willReturn(1L);
        Like like1 = mock(Like.class);
        given(like1.getPost()).willReturn(post1);

        Post post2 = mock(Post.class);
        given(post2.getId()).willReturn(2L);
        Like like2 = mock(Like.class);
        given(like2.getPost()).willReturn(post2);

        List<Like> likes = List.of(like1, like2);
        given(likeRepository.findAllByMemberAndPostIn(member, List.of(refPost1, refPost2, refPost3)))
                .willReturn(likes);

        // when
        List<PostLiked> result = likeService.areLiked(3L, List.of(1L, 2L, 3L));

        // then
        assertThat(result).hasSize(3);
        assertThat(result).extracting("postId").containsExactly(1L, 2L, 3L);
        assertThat(result).extracting("liked").containsExactly(true, true, false);
    }

    @Test
    void isLiked_True() {
        // given
        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        Post post = mock(Post.class);
        given(post.getUseYn()).willReturn(true);
        given(postRepository.findById(2L)).willReturn(Optional.of(post));

        given(likeRepository.existsByPostAndMember(post, member)).willReturn(true);

        // when
        boolean result = likeService.isLiked(1L, 2L);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void isLiked_False() {
        // given
        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        Post post = mock(Post.class);
        given(post.getUseYn()).willReturn(true);
        given(postRepository.findById(2L)).willReturn(Optional.of(post));

        given(likeRepository.existsByPostAndMember(post, member)).willReturn(false);

        // when
        boolean result = likeService.isLiked(1L, 2L);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void getLikedPosts() {
        // given
        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        Post post = mock(Post.class);
        given(post.getId()).willReturn(2L);
        given(post.getContent()).willReturn("content");
        given(post.getCreatedAt()).willReturn(LocalDateTime.of(2022, 10, 31, 0, 21));

        Like like = mock(Like.class);
        given(like.getPost()).willReturn(post);
        given(likeRepository.findSliceWithPostByMember(any(Pageable.class), eq(member)))
                .willReturn(new SliceImpl<>(List.of(like)));

        Image image1 = mock(Image.class);
        given(image1.getUseYn()).willReturn(true);
        given(image1.getUrl()).willReturn("url-1");
        PostImage postImage1 = mock(PostImage.class);
        given(postImage1.getImage()).willReturn(image1);
        given(postImage1.getPost()).willReturn(post);

        Image image2 = mock(Image.class);
        given(image2.getUseYn()).willReturn(false);
        PostImage postImage2 = mock(PostImage.class);
        given(postImage2.getImage()).willReturn(image2);

        given(postImageRepository.findAllWithImageByPostIn(List.of(post)))
                .willReturn(List.of(postImage1, postImage2));

        // when
        PageRequest pageRequest = PageRequest.of(0, 10);
        Slice<LikedPostDto> result = likeService.getLikedPosts(pageRequest, 1L);

        // then
        List<LikedPostDto> content = result.getContent();
        assertThat(content).hasSize(1);
        assertThat(content).extracting("id").containsExactly(2L);
        assertThat(content).extracting("content").containsExactly("content");
        assertThat(content).extracting("createdAt").containsExactly(LocalDateTime.of(2022, 10, 31, 0, 21));

        List<String> imageUrls = content.get(0).getImageUrls();
        assertThat(imageUrls)
                .hasSize(1)
                .containsExactly("url-1");
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

        Like like1 = mock(Like.class);
        given(like1.getPost()).willReturn(post1);
        Like like2 = mock(Like.class);
        given(like2.getPost()).willReturn(post2);

        List<Like> likes = List.of(like1, like2);
        given(likeRepository.findAllByMember(member)).willReturn(likes);

        Post lockedPost1 = mock(Post.class);
        Post lockedPost2 = mock(Post.class);
        List<Post> lockedPosts = List.of(lockedPost1, lockedPost2);
        given(postRepository.findAllWithPessimisticLockByIdIn(List.of(10L, 11L)))
                .willReturn(lockedPosts);

        // when
        likeService.deleteAllOfMember(1L);

        // then
        then(likeRepository).should().deleteAll(likes);

        for (Post lockedPost : lockedPosts) {
            then(lockedPost).should(times(1)).decreaseLikeCount();
        }
    }

}