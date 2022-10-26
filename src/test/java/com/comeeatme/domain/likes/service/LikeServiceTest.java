package com.comeeatme.domain.likes.service;

import com.comeeatme.domain.likes.Likes;
import com.comeeatme.domain.likes.repository.LikesRepository;
import com.comeeatme.domain.likes.response.LikeResult;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
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
    void pushLike_Like() {
        // given
        Post post = mock(Post.class);
        given(post.getUseYn()).willReturn(true);
        given(post.getId()).willReturn(1L);
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findByUsername(anyString())).willReturn(Optional.of(member));

        given(likesRepository.findByPostAndMember(post, member)).willReturn(Optional.empty());

        given(likesRepository.countByPost(post)).willReturn(10L);

        // when
        LikeResult result = likeService.pushLike(1L, "username");

        // then
        ArgumentCaptor<Likes> likesCaptor = ArgumentCaptor.forClass(Likes.class);
        then(likesRepository).should().save(likesCaptor.capture());
        Likes likesCaptorValue = likesCaptor.getValue();
        assertThat(likesCaptorValue.getPost()).isEqualTo(post);
        assertThat(likesCaptorValue.getMember()).isEqualTo(member);

        assertThat(result.getPostId()).isEqualTo(1L);
        assertThat(result.getLiked()).isTrue();
        assertThat(result.getCount()).isEqualTo(10L);
    }

    @Test
    void pushLike_Cancel() {
        // given
        Post post = mock(Post.class);
        given(post.getUseYn()).willReturn(true);
        given(post.getId()).willReturn(1L);
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findByUsername(anyString())).willReturn(Optional.of(member));

        Likes like = mock(Likes.class);
        given(likesRepository.findByPostAndMember(post, member)).willReturn(Optional.of(like));

        given(likesRepository.countByPost(post)).willReturn(10L);

        // when
        LikeResult result = likeService.pushLike(1L, "username");

        // then
        then(likesRepository).should().delete(like);

        assertThat(result.getPostId()).isEqualTo(1L);
        assertThat(result.getLiked()).isFalse();
        assertThat(result.getCount()).isEqualTo(10L);
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