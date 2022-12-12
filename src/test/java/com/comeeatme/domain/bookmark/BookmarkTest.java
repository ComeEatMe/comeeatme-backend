package com.comeeatme.domain.bookmark;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.bookmark.repository.BookmarkRepository;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.post.Post;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Transactional
@Import(TestJpaConfig.class)
class BookmarkTest {

    @Autowired
    private BookmarkRepository bookmarkRepository;
    @Autowired
    private MemberRepository memberRepository;

    @Test
    void createAndSave() {
        assertThatNoException().isThrownBy(() -> bookmarkRepository.save(
                Bookmark.builder()
                        .member(memberRepository.getReferenceById(1L))
                        .post(Post.builder().id(2L).build())
                        .build()
        ));
    }

    @Test
    void Unique_Post_Member() {
        assertThatThrownBy(() -> bookmarkRepository.saveAll(List.of(
                Bookmark.builder()
                        .member(memberRepository.getReferenceById(1L))
                        .post(Post.builder().id(2L).build())
                        .build(),
                Bookmark.builder()
                        .member(memberRepository.getReferenceById(1L))
                        .post(Post.builder().id(2L).build())
                        .build()
        ))).isInstanceOf(DataIntegrityViolationException.class);;
    }

}