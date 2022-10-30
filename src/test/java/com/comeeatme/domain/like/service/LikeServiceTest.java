package com.comeeatme.domain.like.service;

import com.comeeatme.domain.image.Image;
import com.comeeatme.domain.like.Like;
import com.comeeatme.domain.like.repository.LikeRepository;
import com.comeeatme.domain.like.response.LikedPostDto;
import com.comeeatme.domain.like.response.PostLiked;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.post.Post;
import com.comeeatme.domain.post.PostImage;
import com.comeeatme.domain.post.repository.PostImageRepository;
import com.comeeatme.domain.post.repository.PostRepository;
import com.comeeatme.error.exception.AlreadyLikedPostException;
import com.comeeatme.error.exception.EntityNotFoundException;
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
import static org.mockito.Mockito.mock;

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
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findByUsername("username")).willReturn(Optional.of(member));

        given(likeRepository.existsByPostAndMember(post, member)).willReturn(false);

        // when
        likeService.like(1L, "username");

        // then
        ArgumentCaptor<Like> likesCaptor = ArgumentCaptor.forClass(Like.class);
        then(likeRepository).should().save(likesCaptor.capture());
        Like likeCaptorValue = likesCaptor.getValue();
        assertThat(likeCaptorValue.getPost()).isEqualTo(post);
        assertThat(likeCaptorValue.getMember()).isEqualTo(member);
    }

    @Test
    void like_AlreadyLiked() {
        // given
        Post post = mock(Post.class);
        given(post.getUseYn()).willReturn(true);
        given(post.getId()).willReturn(1L);
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findByUsername("username")).willReturn(Optional.of(member));

        given(likeRepository.existsByPostAndMember(post, member)).willReturn(true);

        // expected
        assertThatThrownBy(() -> likeService.like(1L, "username"))
                .isInstanceOf(AlreadyLikedPostException.class);
    }

    @Test
    void unlike() {
        // given
        Post post = mock(Post.class);
        given(post.getUseYn()).willReturn(true);
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findByUsername("username")).willReturn(Optional.of(member));

        Like like = mock(Like.class);
        given(likeRepository.findByPostAndMember(post, member)).willReturn(Optional.of(like));

        // when
        likeService.unlike(1L, "username");

        // then
        then(likeRepository).should().delete(like);
    }

    @Test
    void unlike_NotLiked() {
        // given
        Post post = mock(Post.class);
        given(post.getUseYn()).willReturn(true);
        given(post.getId()).willReturn(1L);
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findByUsername("username")).willReturn(Optional.of(member));

        given(likeRepository.findByPostAndMember(post, member)).willReturn(Optional.empty());

        // expected
        assertThatThrownBy(() -> likeService.unlike(1L, "username"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void areLiked() {
        // given
        Post post1 = mock(Post.class);
        given(post1.getId()).willReturn(1L);
        Like like1 = mock(Like.class);
        given(like1.getPost()).willReturn(post1);

        Post post2 = mock(Post.class);
        given(post2.getId()).willReturn(2L);
        Like like2 = mock(Like.class);
        given(like2.getPost()).willReturn(post2);

        List<Like> likes = List.of(like1, like2);
        given(likeRepository.findByMemberIdAndPostIds(3L, List.of(1L, 2L, 3L)))
                .willReturn(likes);

        // when
        List<PostLiked> result = likeService.areLiked(3L, List.of(1L, 2L, 3L));

        // then
        assertThat(result).hasSize(3);
        assertThat(result).extracting("postId").containsExactly(1L, 2L, 3L);
        assertThat(result).extracting("liked").containsExactly(true, true, false);
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

}