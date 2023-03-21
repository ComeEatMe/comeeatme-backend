package com.comeeatme.domain.bookmark.repository;

import com.comeeatme.domain.bookmark.Bookmark;
import com.comeeatme.domain.common.TestJpaConfig;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.post.repository.PostRepository;
import com.comeeatme.domain.restaurant.repository.RestaurantRepository;
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

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void deleteAllByPost() {
        // given
        List<Bookmark> bookmarks = bookmarkRepository.saveAll(List.of(
                Bookmark.builder()
                        .member(memberRepository.getReferenceById(10L))
                        .post(postRepository.getReferenceById(1L))
                        .build(),
                Bookmark.builder()
                        .member(memberRepository.getReferenceById(10L))
                        .post(postRepository.getReferenceById(2L))
                        .build(),
                Bookmark.builder()
                        .member(memberRepository.getReferenceById(11L))
                        .post(postRepository.getReferenceById(1L))
                        .build()
        ));

        // when
        bookmarkRepository.deleteAllByPost(postRepository.getReferenceById(1L));

        // then
        List<Bookmark> foundBookmarks = bookmarkRepository.findAll();
        assertThat(foundBookmarks)
                .hasSize(1)
                .extracting("id").containsOnly(bookmarks.get(1).getId());
    }

}