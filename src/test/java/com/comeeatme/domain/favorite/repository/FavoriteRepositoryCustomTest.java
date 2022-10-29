package com.comeeatme.domain.favorite.repository;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.favorite.Favorite;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.restaurant.Restaurant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Transactional
@Import(TestJpaConfig.class)
class FavoriteRepositoryCustomTest {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Test
    void findAllByMemberIdAndRestaurantIds() {
        // given
        List<Favorite> favorites = favoriteRepository.saveAll(List.of(
                Favorite.builder()
                        .member(Member.builder().id(10L).build())
                        .restaurant(Restaurant.builder().id(1L).build())
                        .build(),
                Favorite.builder()
                        .member(Member.builder().id(10L).build())
                        .restaurant(Restaurant.builder().id(2L).build())
                        .build(),
                Favorite.builder()
                        .member(Member.builder().id(11L).build())
                        .restaurant(Restaurant.builder().id(3L).build())
                        .build()
        ));

        // when
        List<Favorite> result = favoriteRepository.findAllByMemberIdAndRestaurantIds(10L, List.of(1L, 2L, 3L));

        // then
        assertThat(result)
                .hasSize(2)
                .extracting("id").containsOnly(favorites.get(0).getId(), favorites.get(1).getId());
    }
}