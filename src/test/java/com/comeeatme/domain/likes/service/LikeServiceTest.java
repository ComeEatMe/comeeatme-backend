package com.comeeatme.domain.likes.service;

import com.comeeatme.domain.likes.Likes;
import com.comeeatme.domain.likes.repository.LikesRepository;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.post.Post;
import com.comeeatme.domain.post.repository.PostRepository;
import com.comeeatme.error.exception.AlreadyLikedPostException;
import com.comeeatme.error.exception.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @InjectMocks
    private LikeService likeService;

    @Mock
    private LikesRepository likesRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private MemberRepository memberRepository;

    @Test
    void like() {
        // given
        Post post = mock(Post.class);
        given(post.getUseYn()).willReturn(true);
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findByUsername("username")).willReturn(Optional.of(member));

        given(likesRepository.existsByPostAndMember(post, member)).willReturn(false);

        // when
        likeService.like(1L, "username");

        // then
        ArgumentCaptor<Likes> likesCaptor = ArgumentCaptor.forClass(Likes.class);
        then(likesRepository).should().save(likesCaptor.capture());
        Likes likesCaptorValue = likesCaptor.getValue();
        assertThat(likesCaptorValue.getPost()).isEqualTo(post);
        assertThat(likesCaptorValue.getMember()).isEqualTo(member);
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

        given(likesRepository.existsByPostAndMember(post, member)).willReturn(true);

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

        Likes like = mock(Likes.class);
        given(likesRepository.findByPostAndMember(post, member)).willReturn(Optional.of(like));

        // when
        likeService.unlike(1L, "username");

        // then
        then(likesRepository).should().delete(like);
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

        given(likesRepository.findByPostAndMember(post, member)).willReturn(Optional.empty());

        // expected
        assertThatThrownBy(() -> likeService.unlike(1L, "username"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void isLiked_username() {
        // when
        likeService.isLiked(List.of(1L, 2L), "username");

        // then
        then(likesRepository).should().existsByPostIdsAndUsername(List.of(1L, 2L), "username");
    }

    @Test
    void isLiked_memberId() {
        // when
        likeService.isLiked(List.of(1L, 2L), 3L);

        // then
        then(likesRepository).should().existsByPostIdsAndMemberId(List.of(1L, 2L), 3L);
    }
}