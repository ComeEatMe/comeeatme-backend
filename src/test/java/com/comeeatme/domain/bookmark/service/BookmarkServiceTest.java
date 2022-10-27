package com.comeeatme.domain.bookmark.service;

import com.comeeatme.domain.bookmark.Bookmark;
import com.comeeatme.domain.bookmark.BookmarkGroup;
import com.comeeatme.domain.bookmark.repository.BookmarkGroupRepository;
import com.comeeatme.domain.bookmark.repository.BookmarkRepository;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.post.Post;
import com.comeeatme.domain.post.repository.PostRepository;
import com.comeeatme.error.exception.AlreadyBookmarkedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class BookmarkServiceTest {

    @InjectMocks
    private BookmarkService bookmarkService;

    @Mock
    private BookmarkGroupRepository bookmarkGroupRepository;

    @Mock
    private BookmarkRepository bookmarkRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private MemberRepository memberRepository;

    @Test
    void bookmark_GroupNotNull() {
        // given
        Post post = mock(Post.class);
        given(post.getUseYn()).willReturn(true);
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findByUsername("username")).willReturn(Optional.of(member));

        BookmarkGroup group = mock(BookmarkGroup.class);
        given(bookmarkGroupRepository.findByMemberAndName(member, "그루비룸")).willReturn(Optional.of(group));

        given(bookmarkRepository.existsByGroupAndPost(group, post)).willReturn(false);

        // when
        bookmarkService.bookmark(1L, "username", "그루비룸");

        // then
        ArgumentCaptor<Bookmark> bookmarkCaptor = ArgumentCaptor.forClass(Bookmark.class);
        then(bookmarkRepository).should().save(bookmarkCaptor.capture());

        Bookmark captorValue = bookmarkCaptor.getValue();
        assertThat(captorValue.getMember()).isEqualTo(member);
        assertThat(captorValue.getGroup()).isEqualTo(group);
        assertThat(captorValue.getPost()).isEqualTo(post);
    }

    @Test
    void bookmark_GroupNull() {
        // given
        Post post = mock(Post.class);
        given(post.getUseYn()).willReturn(true);
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findByUsername("username")).willReturn(Optional.of(member));

        given(bookmarkRepository.existsByGroupAndPost(null, post)).willReturn(false);

        // when
        bookmarkService.bookmark(1L, "username", null);

        // then
        ArgumentCaptor<Bookmark> bookmarkCaptor = ArgumentCaptor.forClass(Bookmark.class);
        then(bookmarkRepository).should().save(bookmarkCaptor.capture());

        Bookmark captorValue = bookmarkCaptor.getValue();
        assertThat(captorValue.getMember()).isEqualTo(member);
        assertThat(captorValue.getGroup()).isNull();
        assertThat(captorValue.getPost()).isEqualTo(post);
    }

    @Test
    void bookmark_AlreadyBookmarked() {
        // given
        Post post = mock(Post.class);
        given(post.getUseYn()).willReturn(true);
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findByUsername("username")).willReturn(Optional.of(member));

        BookmarkGroup group = mock(BookmarkGroup.class);
        given(bookmarkGroupRepository.findByMemberAndName(member, "그루비룸")).willReturn(Optional.of(group));

        given(bookmarkRepository.existsByGroupAndPost(group, post)).willReturn(true);

        // expected
        assertThatThrownBy(() -> bookmarkService.bookmark(1L, "username", "그루비룸"))
                .isInstanceOf(AlreadyBookmarkedException.class);

    }
}