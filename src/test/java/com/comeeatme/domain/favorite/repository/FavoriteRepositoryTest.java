package com.comeeatme.domain.favorite.repository;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.favorite.Favorite;
import com.comeeatme.domain.favorite.FavoriteGroup;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.restaurant.Restaurant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
@Import(TestJpaConfig.class)
class FavoriteRepositoryTest {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Test
    void existsByGroupAndRestaurant() {
        // given
        favoriteRepository.save(Favorite.builder()
                .member(Member.builder().id(1L).build())
                .restaurant(Restaurant.builder().id(2L).build())
                .group(FavoriteGroup.builder().id(3L).build())
                .build());

        // expected
        assertThat(favoriteRepository.existsByGroupAndRestaurant(
                FavoriteGroup.builder().id(3L).build(),
                Restaurant.builder().id(2L).build()
        )).isTrue();
    }

    @Test
    void existsByGroupAndRestaurant_GroupNotEqual() {
        // given
        favoriteRepository.save(Favorite.builder()
                .member(Member.builder().id(1L).build())
                .restaurant(Restaurant.builder().id(2L).build())
                .group(FavoriteGroup.builder().id(3L).build())
                .build());

        // expected
        assertThat(favoriteRepository.existsByGroupAndRestaurant(
                FavoriteGroup.builder().id(4L).build(),
                Restaurant.builder().id(2L).build()
        )).isFalse();
    }

    @Test
    void existsByGroupAndRestaurant_RestaurantNotEqual() {
        // given
        favoriteRepository.save(Favorite.builder()
                .member(Member.builder().id(1L).build())
                .restaurant(Restaurant.builder().id(2L).build())
                .group(FavoriteGroup.builder().id(3L).build())
                .build());

        // expected
        assertThat(favoriteRepository.existsByGroupAndRestaurant(
                FavoriteGroup.builder().id(3L).build(),
                Restaurant.builder().id(4L).build()
        )).isFalse();
    }

}