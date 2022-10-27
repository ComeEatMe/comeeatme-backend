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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Transactional
@Import(TestJpaConfig.class)
class BookmarkRepositoryTest {

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Test
    void existsByGroupAndPost() {
        // given
        bookmarkRepository.save(Bookmark.builder()
                .member(Member.builder().id(10L).build())
                .post(Post.builder().id(20L).build())
                .group(BookmarkGroup.builder().id(30L).build())
                .build());

        // expected
        assertThat(bookmarkRepository.existsByGroupAndPost(
                BookmarkGroup.builder().id(30L).build(),
                Post.builder().id(20L).build()
        )).isTrue();
    }

    @Test
    void existsByGroupAndPost_GroupNull() {
        // given
        bookmarkRepository.save(Bookmark.builder()
                .member(Member.builder().id(10L).build())
                .post(Post.builder().id(20L).build())
                .build());

        // expected
        assertThat(bookmarkRepository.existsByGroupAndPost(
                null,
                Post.builder().id(20L).build()
        )).isTrue();
    }

    @Test
    void existsByGroupAndPost_PostNotEqual() {
        // given
        bookmarkRepository.save(Bookmark.builder()
                .member(Member.builder().id(10L).build())
                .post(Post.builder().id(20L).build())
                .group(BookmarkGroup.builder().id(30L).build())
                .build());

        // expected
        assertThat(bookmarkRepository.existsByGroupAndPost(
                BookmarkGroup.builder().id(30L).build(),
                Post.builder().id(40L).build()
        )).isFalse();
    }

    @Test
    void existsByGroupAndPost_GroupNotEqual() {
        // given
        bookmarkRepository.save(Bookmark.builder()
                .member(Member.builder().id(10L).build())
                .post(Post.builder().id(20L).build())
                .group(BookmarkGroup.builder().id(30L).build())
                .build());

        // expected
        assertThat(bookmarkRepository.existsByGroupAndPost(
                BookmarkGroup.builder().id(400L).build(),
                Post.builder().id(20L).build()
        )).isFalse();
    }

}