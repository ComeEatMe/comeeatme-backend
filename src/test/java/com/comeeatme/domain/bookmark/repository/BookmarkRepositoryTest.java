package com.comeeatme.domain.bookmark.repository;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.bookmark.Bookmark;
import com.comeeatme.domain.bookmark.BookmarkGroup;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.post.Post;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
@Import(TestJpaConfig.class)
class BookmarkRepositoryTest {

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Test
    void existsByMemberAndGroupAndPost() {
        // given
        bookmarkRepository.save(Bookmark.builder()
                .member(Member.builder().id(10L).build())
                .post(Post.builder().id(20L).build())
                .group(BookmarkGroup.builder().id(30L).build())
                .build());

        // expected
        assertThat(bookmarkRepository.existsByMemberAndGroupAndPost(
                Member.builder().id(10L).build(),
                BookmarkGroup.builder().id(30L).build(),
                Post.builder().id(20L).build()
        )).isTrue();
    }

    @Test
    void existsByMemberAndGroupAndPost_GroupNull() {
        // given
        bookmarkRepository.save(Bookmark.builder()
                .member(Member.builder().id(10L).build())
                .post(Post.builder().id(20L).build())
                .build());

        // expected
        assertThat(bookmarkRepository.existsByMemberAndGroupAndPost(
                Member.builder().id(10L).build(),
                null,
                Post.builder().id(20L).build()
        )).isTrue();
    }

    @Test
    void existsByMemberAndGroupAndPost_PostNotEqual() {
        // given
        bookmarkRepository.save(Bookmark.builder()
                .member(Member.builder().id(10L).build())
                .post(Post.builder().id(20L).build())
                .group(BookmarkGroup.builder().id(30L).build())
                .build());

        // expected
        assertThat(bookmarkRepository.existsByMemberAndGroupAndPost(
                Member.builder().id(10L).build(),
                BookmarkGroup.builder().id(30L).build(),
                Post.builder().id(40L).build()
        )).isFalse();
    }

    @Test
    void existsByMemberAndGroupAndPost_GroupNotEqual() {
        // given
        bookmarkRepository.save(Bookmark.builder()
                .member(Member.builder().id(10L).build())
                .post(Post.builder().id(20L).build())
                .group(BookmarkGroup.builder().id(30L).build())
                .build());

        // expected
        assertThat(bookmarkRepository.existsByMemberAndGroupAndPost(
                Member.builder().id(10L).build(),
                BookmarkGroup.builder().id(400L).build(),
                Post.builder().id(20L).build()
        )).isFalse();
    }

    @Test
    void findByGroupAndPost() {
        // given
        Bookmark bookmark = bookmarkRepository.save(Bookmark.builder()
                .member(Member.builder().id(10L).build())
                .post(Post.builder().id(20L).build())
                .group(BookmarkGroup.builder().id(30L).build())
                .build());

        // when
        Bookmark result = bookmarkRepository.findByGroupAndPost(
                BookmarkGroup.builder().id(30L).build(),
                Post.builder().id(20L).build()
        ).orElseThrow();

        // then
        assertThat(bookmark.getId()).isEqualTo(result.getId());
    }

    @Test
    void findByGroupAndPost_GroupNotEqual() {
        // given
        Bookmark bookmark = bookmarkRepository.save(Bookmark.builder()
                .member(Member.builder().id(10L).build())
                .post(Post.builder().id(20L).build())
                .group(BookmarkGroup.builder().id(30L).build())
                .build());

        // expected
        assertThat(bookmarkRepository.findByGroupAndPost(
                BookmarkGroup.builder().id(40L).build(),
                Post.builder().id(20L).build()
        )).isEmpty();
    }

    @Test
    void findByGroupAndPost_PostNotEqual() {
        // given
        Bookmark bookmark = bookmarkRepository.save(Bookmark.builder()
                .member(Member.builder().id(10L).build())
                .post(Post.builder().id(20L).build())
                .group(BookmarkGroup.builder().id(30L).build())
                .build());

        // expected
        assertThat(bookmarkRepository.findByGroupAndPost(
                BookmarkGroup.builder().id(30L).build(),
                Post.builder().id(40L).build()
        )).isEmpty();
    }

    @Test
    void countByMember() {
        // given
        bookmarkRepository.saveAll(List.of(
                Bookmark.builder()
                        .member(Member.builder().id(1L).build())
                        .post(Post.builder().id(2L).build())
                        .group(BookmarkGroup.builder().id(3L).build())
                        .build(),
                Bookmark.builder()
                        .member(Member.builder().id(1L).build())
                        .post(Post.builder().id(3L).build())
                        .group(BookmarkGroup.builder().id(3L).build())
                        .build(),
                Bookmark.builder()
                        .member(Member.builder().id(2L).build())
                        .post(Post.builder().id(4L).build())
                        .group(BookmarkGroup.builder().id(3L).build())
                        .build()
        ));

        // when
        int result = bookmarkRepository.countByMember(Member.builder().id(1L).build());

        // then
        assertThat(result).isEqualTo(2);
    }

    @Test
    void existsByMemberAndPost() {
        // given
        bookmarkRepository.save(Bookmark.builder()
                .member(Member.builder().id(1L).build())
                .post(Post.builder().id(2L).build())
                .build());

        // expected
        assertThat(bookmarkRepository.existsByMemberAndPost(
                Member.builder().id(1L).build(),
                Post.builder().id(2L).build()
        )).isTrue();
    }

    @Test
    void existsByMemberAndPost_MemberNotEqual() {
        // given
        bookmarkRepository.save(Bookmark.builder()
                .member(Member.builder().id(1L).build())
                .post(Post.builder().id(2L).build())
                .build());

        // expected
        assertThat(bookmarkRepository.existsByMemberAndPost(
                Member.builder().id(3L).build(),
                Post.builder().id(2L).build()
        )).isFalse();
    }

    @Test
    void existsByMemberAndPost_PostNotEqual() {
        // given
        bookmarkRepository.save(Bookmark.builder()
                .member(Member.builder().id(1L).build())
                .post(Post.builder().id(2L).build())
                .build());

        // expected
        assertThat(bookmarkRepository.existsByMemberAndPost(
                Member.builder().id(1L).build(),
                Post.builder().id(3L).build()
        )).isFalse();
    }

}