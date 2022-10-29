package com.comeeatme.domain.bookmark.service;

import com.comeeatme.domain.bookmark.Bookmark;
import com.comeeatme.domain.bookmark.BookmarkGroup;
import com.comeeatme.domain.bookmark.repository.BookmarkGroupRepository;
import com.comeeatme.domain.bookmark.repository.BookmarkRepository;
import com.comeeatme.domain.bookmark.response.BookmarkGroupDto;
import com.comeeatme.domain.bookmark.response.PostBookmarked;
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

import java.util.List;
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

        then(group).should().incrBookmarkCount();
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

    @Test
    void cancelBookmark() {
        // given
        Post post = mock(Post.class);
        given(post.getUseYn()).willReturn(true);
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findByUsername("username")).willReturn(Optional.of(member));

        BookmarkGroup group = mock(BookmarkGroup.class);
        given(bookmarkGroupRepository.findByMemberAndName(member, "그루비룸")).willReturn(Optional.of(group));

        Bookmark bookmark = mock(Bookmark.class);
        given(bookmarkRepository.findByGroupAndPost(group, post)).willReturn(Optional.of(bookmark));

        // when
        bookmarkService.cancelBookmark(1L, "username", "그루비룸");

        // then
        then(bookmarkRepository).should().delete(bookmark);

        then(group).should().decrBookmarkCount();
    }

    @Test
    void getAllGroupsOfMember() {
        // given
        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        BookmarkGroup group1 = mock(BookmarkGroup.class);
        given(group1.getName()).willReturn("그루비룸-1");
        given(group1.getBookmarkCount()).willReturn(1);

        BookmarkGroup group2 = mock(BookmarkGroup.class);
        given(group2.getName()).willReturn("그루비룸-2");
        given(group2.getBookmarkCount()).willReturn(2);
        given(bookmarkGroupRepository.findAllByMember(member)).willReturn(List.of(group1, group2));

        given(bookmarkRepository.countByMember(member)).willReturn(10);

        // when
        List<BookmarkGroupDto> result = bookmarkService.getAllGroupsOfMember(1L);

        // then
        assertThat(result).hasSize(3);
        assertThat(result).extracting("name").containsExactly(BookmarkGroup.ALL_NAME, "그루비룸-1", "그루비룸-2");
        assertThat(result).extracting("bookmarkCount").containsExactly(10, 1, 2);
    }

    @Test
    void isBookmarked() {
        // given
        Post post1 = mock(Post.class);
        given(post1.getId()).willReturn(1L);
        Bookmark bookmark1 = mock(Bookmark.class);
        given(bookmark1.getPost()).willReturn(post1);

        Post post2 = mock(Post.class);
        given(post2.getId()).willReturn(2L);
        Bookmark bookmark2 = mock(Bookmark.class);
        given(bookmark2.getPost()).willReturn(post2);

        List<Bookmark> bookmarks = List.of(bookmark1, bookmark2);
        given(bookmarkRepository.findByMemberIdAndPostIds(3L, List.of(1L, 2L, 3L)))
                .willReturn(bookmarks);

        // when
        List<PostBookmarked> result = bookmarkService.isBookmarked(3L, List.of(1L, 2L, 3L));

        // then
        assertThat(result).hasSize(3);
        assertThat(result).extracting("postId").containsExactly(1L, 2L, 3L);
        assertThat(result).extracting("bookmarked").containsExactly(true, true, false);
    }

}