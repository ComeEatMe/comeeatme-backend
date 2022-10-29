package com.comeeatme.domain.bookmark.repository;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.bookmark.Bookmark;
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
class BookmarkRepositoryCustomTest {

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Test
    void findByMemberIdAndPostIds() {
        // given
        List<Bookmark> bookmarks = bookmarkRepository.saveAll(List.of(
                Bookmark.builder()
                        .member(Member.builder().id(10L).build())
                        .post(Post.builder().id(1L).build())
                        .build(),
                Bookmark.builder()
                        .member(Member.builder().id(10L).build())
                        .post(Post.builder().id(2L).build())
                        .build(),
                Bookmark.builder()
                        .member(Member.builder().id(11L).build())
                        .post(Post.builder().id(3L).build())
                        .build()
        ));

        // when
        List<Bookmark> result = bookmarkRepository.findByMemberIdAndPostIds(10L, List.of(1L, 2L, 3L));

        // then
        result.sort((o1, o2) -> (int) (o1.getPost().getId() - o2.getPost().getId()));
        assertThat(result)
                .hasSize(2)
                .extracting("id").containsExactly(bookmarks.get(0).getId(), bookmarks.get(1).getId());
    }

    @Test
    void deleteAllByPost() {
        // given
        List<Bookmark> bookmarks = bookmarkRepository.saveAll(List.of(
                Bookmark.builder()
                        .member(Member.builder().id(10L).build())
                        .post(Post.builder().id(1L).build())
                        .build(),
                Bookmark.builder()
                        .member(Member.builder().id(10L).build())
                        .post(Post.builder().id(2L).build())
                        .build(),
                Bookmark.builder()
                        .member(Member.builder().id(11L).build())
                        .post(Post.builder().id(1L).build())
                        .build()
        ));

        // when
        bookmarkRepository.deleteAllByPost(Post.builder().id(1L).build());

        // then
        List<Bookmark> foundBookmarks = bookmarkRepository.findAll();
        assertThat(foundBookmarks)
                .hasSize(1)
                .extracting("id").containsOnly(bookmarks.get(1).getId());
    }

}