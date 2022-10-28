package com.comeeatme.domain.bookmark.repository;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.bookmark.BookmarkGroup;
import com.comeeatme.domain.member.Member;
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
class BookmarkGroupRepositoryTest {

    @Autowired
    private BookmarkGroupRepository bookmarkGroupRepository;

    @Test
    void findByMemberAndName() {
        // given
        BookmarkGroup bookmarkGroup = bookmarkGroupRepository.save(BookmarkGroup.builder()
                .name("그루비룸")
                .bookmarkCount(0)
                .member(Member.builder().id(10L).build())
                .build());

        // when
        BookmarkGroup result = bookmarkGroupRepository.findByMemberAndName(
                Member.builder().id(10L).build(), "그루비룸"
        ).orElseThrow();

        // then
        assertThat(result.getId()).isEqualTo(bookmarkGroup.getId());
    }

    @Test
    void findByMemberAndName_MemberNotEqual() {
        // given
        BookmarkGroup bookmarkGroup = bookmarkGroupRepository.save(BookmarkGroup.builder()
                .name("그루비룸")
                .bookmarkCount(0)
                .member(Member.builder().id(10L).build())
                .build());

        // when
        assertThat(bookmarkGroupRepository.findByMemberAndName(
                Member.builder().id(20L).build(), "그루비룸"
        )).isEmpty();
    }

    @Test
    void findByMemberAndName_NameNotEqual() {
        // given
        BookmarkGroup bookmarkGroup = bookmarkGroupRepository.save(BookmarkGroup.builder()
                .name("그루비룸")
                .bookmarkCount(0)
                .member(Member.builder().id(10L).build())
                .build());

        // when
        assertThat(bookmarkGroupRepository.findByMemberAndName(
                Member.builder().id(10L).build(), "그루"
        )).isEmpty();
    }

    @Test
    void findAllByMember() {
        // given
        bookmarkGroupRepository.saveAll(List.of(
                BookmarkGroup.builder()
                        .name("그루비룸-1")
                        .bookmarkCount(0)
                        .member(Member.builder().id(10L).build())
                        .build(),
                BookmarkGroup.builder()
                        .name("그루비룸-2")
                        .bookmarkCount(0)
                        .member(Member.builder().id(20L).build())
                        .build()
        ));

        // when
        List<BookmarkGroup> result = bookmarkGroupRepository.findAllByMember(Member.builder().id(10L).build());

        // then
        assertThat(result)
                .hasSize(1)
                .extracting("name").containsExactly("그루비룸-1");
    }

}