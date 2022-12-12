package com.comeeatme.domain.bookmark.service;

import com.comeeatme.domain.bookmark.Bookmark;
import com.comeeatme.domain.bookmark.repository.BookmarkRepository;
import com.comeeatme.domain.bookmark.response.BookmarkedPostDto;
import com.comeeatme.domain.bookmark.response.PostBookmarked;
import com.comeeatme.domain.image.Image;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.post.Post;
import com.comeeatme.domain.post.PostImage;
import com.comeeatme.domain.post.repository.PostImageRepository;
import com.comeeatme.domain.post.repository.PostRepository;
import com.comeeatme.domain.restaurant.Restaurant;
import com.comeeatme.error.exception.AlreadyBookmarkedException;
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
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class BookmarkServiceTest {

    @InjectMocks
    private BookmarkService bookmarkService;

    @Mock
    private BookmarkRepository bookmarkRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PostImageRepository postImageRepository;

    @Test
    void bookmark_GroupNull() {
        // given
        Post post = mock(Post.class);
        given(post.getUseYn()).willReturn(true);
        given(postRepository.findWithPessimisticLockById(1L)).willReturn(Optional.of(post));

        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findById(2L)).willReturn(Optional.of(member));

        given(bookmarkRepository.existsByPostAndMember(post, member)).willReturn(false);

        // when
        bookmarkService.bookmark(1L, 2L);

        // then
        ArgumentCaptor<Bookmark> bookmarkCaptor = ArgumentCaptor.forClass(Bookmark.class);
        then(bookmarkRepository).should().save(bookmarkCaptor.capture());

        Bookmark captorValue = bookmarkCaptor.getValue();
        assertThat(captorValue.getMember()).isEqualTo(member);
        assertThat(captorValue.getPost()).isEqualTo(post);

        then(post).should().increaseBookmarkCount();
    }

    @Test
    void bookmark_AlreadyBookmarked() {
        // given
        Post post = mock(Post.class);
        given(post.getUseYn()).willReturn(true);
        given(postRepository.findWithPessimisticLockById(1L)).willReturn(Optional.of(post));

        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findById(2L)).willReturn(Optional.of(member));

        given(bookmarkRepository.existsByPostAndMember(post, member)).willReturn(true);

        // expected
        assertThatThrownBy(() -> bookmarkService.bookmark(1L, 2L))
                .isInstanceOf(AlreadyBookmarkedException.class);
    }

    @Test
    void cancelBookmark() {
        // given
        Post post = mock(Post.class);
        given(post.getUseYn()).willReturn(true);
        given(postRepository.findWithPessimisticLockById(1L)).willReturn(Optional.of(post));

        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findById(2L)).willReturn(Optional.of(member));

        Bookmark bookmark = mock(Bookmark.class);
        given(bookmarkRepository.findByPostAndMember(post, member)).willReturn(Optional.of(bookmark));

        // when
        bookmarkService.cancelBookmark(1L, 2L);

        // then
        then(bookmarkRepository).should().delete(bookmark);

        then(post).should().decreaseBookmarkCount();
    }

    @Test
    void areBookmarked() {
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
        Bookmark bookmark1 = mock(Bookmark.class);
        given(bookmark1.getPost()).willReturn(post1);

        Post post2 = mock(Post.class);
        given(post2.getId()).willReturn(2L);
        Bookmark bookmark2 = mock(Bookmark.class);
        given(bookmark2.getPost()).willReturn(post2);

        List<Bookmark> bookmarks = List.of(bookmark1, bookmark2);
        given(bookmarkRepository.findAllByMemberAndPostIn(member, List.of(refPost1, refPost2, refPost3)))
                .willReturn(bookmarks);

        // when
        List<PostBookmarked> result = bookmarkService.areBookmarked(3L, List.of(1L, 2L, 3L));

        // then
        assertThat(result).hasSize(3);
        assertThat(result).extracting("postId").containsExactly(1L, 2L, 3L);
        assertThat(result).extracting("bookmarked").containsExactly(true, true, false);
    }

    @Test
    void isBookmarked_True() {
        // given
        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        Post post = mock(Post.class);
        given(post.getUseYn()).willReturn(true);
        given(postRepository.findById(2L)).willReturn(Optional.of(post));

        given(bookmarkRepository.existsByPostAndMember(post, member)).willReturn(true);

        // when
        boolean result = bookmarkService.isBookmarked(1L, 2L);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void isBookmarked_False() {
        // given
        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        Post post = mock(Post.class);
        given(post.getUseYn()).willReturn(true);
        given(postRepository.findById(2L)).willReturn(Optional.of(post));

        given(bookmarkRepository.existsByPostAndMember(post, member)).willReturn(false);

        // when
        boolean result = bookmarkService.isBookmarked(1L, 2L);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void getBookmarkedPosts_DeletedImageContain() {
        // given
        Image memberImage = mock(Image.class);
        given(memberImage.getUseYn()).willReturn(true);
        given(memberImage.getUrl()).willReturn("member-image-url");
        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(member.getId()).willReturn(1L);
        given(member.getNickname()).willReturn("nickname");
        given(member.getImage()).willReturn(memberImage);
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        Restaurant restaurant = mock(Restaurant.class);
        given(restaurant.getId()).willReturn(2L);
        given(restaurant.getName()).willReturn("지그재그");

        Post post = mock(Post.class);
        given(post.getId()).willReturn(3L);
        given(post.getContent()).willReturn("content");
        given(post.getCreatedAt()).willReturn(LocalDateTime.of(2022, 10, 29, 19, 59));
        given(post.getRestaurant()).willReturn(restaurant);
        given(post.getMember()).willReturn(member);

        Bookmark bookmark = mock(Bookmark.class);
        given(bookmark.getPost()).willReturn(post);
        given(bookmarkRepository.findSliceWithByMember(any(Pageable.class), eq(member)))
                .willReturn(new SliceImpl<>(List.of(bookmark)));

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
        Slice<BookmarkedPostDto> result = bookmarkService.getBookmarkedPosts(pageRequest, 1L);

        // then
        List<BookmarkedPostDto> content = result.getContent();
        assertThat(content).hasSize(1);
        assertThat(content).extracting("id").containsExactly(3L);
        assertThat(content).extracting("content").containsExactly("content");
        assertThat(content).extracting("createdAt").containsExactly(LocalDateTime.of(2022, 10, 29, 19, 59));
        assertThat(content).extracting("member.id").containsExactly(1L);
        assertThat(content).extracting("member.nickname").containsExactly("nickname");
        assertThat(content).extracting("member.imageUrl").containsExactly("member-image-url");
        assertThat(content).extracting("restaurant.id").containsExactly(2L);
        assertThat(content).extracting("restaurant.name").containsExactly("지그재그");
        for (BookmarkedPostDto bookmarkedPostDto : content) {
            assertThat(bookmarkedPostDto.getImageUrls())
                    .containsExactly("url-1");
        }
    }

    @Test
    void getBookmarkedPosts_MemberImageNull_GroupNull() {
        // given
        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(member.getId()).willReturn(1L);
        given(member.getNickname()).willReturn("nickname");
        given(member.getImage()).willReturn(null);
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        Restaurant restaurant = mock(Restaurant.class);
        given(restaurant.getId()).willReturn(2L);
        given(restaurant.getName()).willReturn("지그재그");

        Post post = mock(Post.class);
        given(post.getId()).willReturn(3L);
        given(post.getContent()).willReturn("content");
        given(post.getCreatedAt()).willReturn(LocalDateTime.of(2022, 10, 29, 19, 59));
        given(post.getRestaurant()).willReturn(restaurant);
        given(post.getMember()).willReturn(member);

        Bookmark bookmark = mock(Bookmark.class);
        given(bookmark.getPost()).willReturn(post);
        given(bookmarkRepository.findSliceWithByMember(any(Pageable.class), eq(member)))
                .willReturn(new SliceImpl<>(List.of(bookmark)));

        Image image1 = mock(Image.class);
        given(image1.getUseYn()).willReturn(true);
        given(image1.getUrl()).willReturn("url-1");
        PostImage postImage1 = mock(PostImage.class);
        given(postImage1.getImage()).willReturn(image1);
        given(postImage1.getPost()).willReturn(post);

        Image image2 = mock(Image.class);
        given(image2.getUseYn()).willReturn(true);
        given(image2.getUrl()).willReturn("url-2");
        PostImage postImage2 = mock(PostImage.class);
        given(postImage2.getImage()).willReturn(image2);
        given(postImage2.getPost()).willReturn(post);

        given(postImageRepository.findAllWithImageByPostIn(List.of(post)))
                .willReturn(List.of(postImage1, postImage2));

        // when
        PageRequest pageRequest = PageRequest.of(0, 10);
        Slice<BookmarkedPostDto> result = bookmarkService.getBookmarkedPosts(pageRequest, 1L);

        // then
        List<BookmarkedPostDto> content = result.getContent();
        assertThat(content).hasSize(1);
        assertThat(content).extracting("id").containsExactly(3L);
        assertThat(content).extracting("content").containsExactly("content");
        assertThat(content).extracting("createdAt").containsExactly(LocalDateTime.of(2022, 10, 29, 19, 59));
        assertThat(content).extracting("member.id").containsExactly(1L);
        assertThat(content).extracting("member.nickname").containsExactly("nickname");
        assertThat(content).extracting("member.imageUrl").containsExactly((Object) null);
        assertThat(content).extracting("restaurant.id").containsExactly(2L);
        assertThat(content).extracting("restaurant.name").containsExactly("지그재그");
        for (BookmarkedPostDto bookmarkedPostDto : content) {
            assertThat(bookmarkedPostDto.getImageUrls())
                    .containsExactly("url-1", "url-2");
        }
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

        Bookmark bookmark1 = mock(Bookmark.class);
        given(bookmark1.getPost()).willReturn(post1);
        Bookmark bookmark2 = mock(Bookmark.class);
        given(bookmark2.getPost()).willReturn(post2);

        List<Bookmark> bookmarks = List.of(bookmark1, bookmark2);
        given(bookmarkRepository.findAllByMember(member)).willReturn(bookmarks);

        Post lockedPost1 = mock(Post.class);
        Post lockedPost2 = mock(Post.class);
        List<Post> lockedPosts = List.of(lockedPost1, lockedPost2);
        given(postRepository.findAllWithPessimisticLockByIdIn(List.of(10L, 11L)))
                .willReturn(lockedPosts);

        // when
        bookmarkService.deleteAllOfMember(1L);

        // then
        then(bookmarkRepository).should().deleteAll(bookmarks);

        for (Post lockedPost : lockedPosts) {
            then(lockedPost).should(times(1)).decreaseBookmarkCount();
        }
    }

}