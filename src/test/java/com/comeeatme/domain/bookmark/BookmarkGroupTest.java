package com.comeeatme.domain.bookmark;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.bookmark.repository.BookmarkGroupRepository;
import com.comeeatme.domain.member.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Transactional
@Import(TestJpaConfig.class)
class BookmarkGroupTest {

    @Autowired
    private BookmarkGroupRepository bookmarkGroupRepository;

    @Test
    void createAndSave() {
        assertThatNoException().isThrownBy(() -> bookmarkGroupRepository.save(
                BookmarkGroup.builder()
                        .member(Member.builder().id(1L).build())
                        .name("데이트")
                        .bookmarkCount(0)
                        .build())
        );
    }

    @Test
    void Unique_Member_Name() {
        assertThatThrownBy(() -> bookmarkGroupRepository.saveAll(List.of(
                BookmarkGroup.builder()
                        .member(Member.builder().id(1L).build())
                        .name("데이트")
                        .bookmarkCount(0)
                        .build(),
                BookmarkGroup.builder()
                        .member(Member.builder().id(1L).build())
                        .name("데이트")
                        .bookmarkCount(0)
                        .build()
                ))).isInstanceOf(DataIntegrityViolationException.class);;
    }

    @Test
    void incrBookmarkCount() {
        // given
        BookmarkGroup group = BookmarkGroup.builder()
                .member(Member.builder().id(1L).build())
                .name("그루비룸")
                .bookmarkCount(10)
                .build();

        // when
        group.incrBookmarkCount();

        // then
        assertThat(group.getBookmarkCount()).isEqualTo(11);
    }

    @Test
    void decrBookmarkCount() {
        // given
        BookmarkGroup group = BookmarkGroup.builder()
                .member(Member.builder().id(1L).build())
                .name("그루비룸")
                .bookmarkCount(10)
                .build();

        // when
        group.decrBookmarkCount();

        // then
        assertThat(group.getBookmarkCount()).isEqualTo(9);
    }

}