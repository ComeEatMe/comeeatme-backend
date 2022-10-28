package com.comeeatme.domain.favorite.repository;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.favorite.FavoriteGroup;
import com.comeeatme.domain.member.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
@Import(TestJpaConfig.class)
class FavoriteGroupRepositoryTest {

    @Autowired
    private FavoriteGroupRepository favoriteGroupRepository;

    @Test
    void findByMemberAndName() {
        // given
        FavoriteGroup favoriteGroup = favoriteGroupRepository.save(FavoriteGroup.builder()
                .name("그루비룸")
                .favoriteCount(0)
                .member(Member.builder().id(1L).build())
                .build());

        // when
        FavoriteGroup result = favoriteGroupRepository.findByMemberAndName(
                Member.builder().id(1L).build(), "그루비룸"
        ).orElseThrow();

        // then
        assertThat(result.getId()).isEqualTo(favoriteGroup.getId());
    }

    @Test
    void findByMemberAndName_MemberNotEqual() {
        // given
        FavoriteGroup favoriteGroup = favoriteGroupRepository.save(FavoriteGroup.builder()
                .name("그루비룸")
                .favoriteCount(0)
                .member(Member.builder().id(1L).build())
                .build());

        // expected
        assertThat(favoriteGroupRepository.findByMemberAndName(
                Member.builder().id(2L).build(), "그루비룸"
        )).isEmpty();
    }

    @Test
    void findByMemberAndName_NameNotEqual() {
        // given
        FavoriteGroup favoriteGroup = favoriteGroupRepository.save(FavoriteGroup.builder()
                .name("그루비룸")
                .favoriteCount(0)
                .member(Member.builder().id(1L).build())
                .build());

        // expected
        assertThat(favoriteGroupRepository.findByMemberAndName(
                Member.builder().id(2L).build(), "그루"
        )).isEmpty();
    }

}