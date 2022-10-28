package com.comeeatme.domain.favorite;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.favorite.repository.FavoriteGroupRepository;
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
class FavoriteGroupTest {

    @Autowired
    private FavoriteGroupRepository favoriteGroupRepository;

    @Test
    void createAndSave() {
        assertThatNoException().isThrownBy(() -> favoriteGroupRepository.save(
                FavoriteGroup.builder()
                        .member(Member.builder().id(1L).build())
                        .name("그루비룸")
                        .favoriteCount(0)
                        .build()
        ));
    }

    @Test
    void Unique_Member_Name() {
        assertThatThrownBy(() -> favoriteGroupRepository.saveAll(List.of(
                FavoriteGroup.builder()
                        .member(Member.builder().id(1L).build())
                        .name("그루비룸")
                        .favoriteCount(0)
                        .build(),
                FavoriteGroup.builder()
                        .member(Member.builder().id(1L).build())
                        .name("그루비룸")
                        .favoriteCount(0)
                        .build()
        ))).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void incrFavoriteCount() {
        // given
        FavoriteGroup group = FavoriteGroup.builder()
                .member(Member.builder().id(1L).build())
                .name("그루비룸")
                .favoriteCount(10)
                .build();

        // when
        group.incrFavoriteCount();

        // then
        assertThat(group.getFavoriteCount()).isEqualTo(11);
    }

    @Test
    void decrFavoriteCount() {
        // given
        FavoriteGroup group = FavoriteGroup.builder()
                .member(Member.builder().id(1L).build())
                .name("그루비룸")
                .favoriteCount(10)
                .build();

        // when
        group.incrFavoriteCount();

        // then
        assertThat(group.getFavoriteCount()).isEqualTo(11);
    }

}