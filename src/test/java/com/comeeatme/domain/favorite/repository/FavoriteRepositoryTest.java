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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
@Import(TestJpaConfig.class)
class FavoriteRepositoryTest {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Test
    void existsByMemberAndGroupAndRestaurant() {
        // given
        favoriteRepository.save(Favorite.builder()
                .member(Member.builder().id(1L).build())
                .restaurant(Restaurant.builder().id(2L).build())
                .group(FavoriteGroup.builder().id(3L).build())
                .build());

        // expected
        assertThat(favoriteRepository.existsByMemberAndGroupAndRestaurant(
                Member.builder().id(1L).build(),
                FavoriteGroup.builder().id(3L).build(),
                Restaurant.builder().id(2L).build()
        )).isTrue();
    }

    @Test
    void existsByMemberAndGroupAndRestaurant_GroupNotEqual() {
        // given
        favoriteRepository.save(Favorite.builder()
                .member(Member.builder().id(1L).build())
                .restaurant(Restaurant.builder().id(2L).build())
                .group(FavoriteGroup.builder().id(3L).build())
                .build());

        // expected
        assertThat(favoriteRepository.existsByMemberAndGroupAndRestaurant(
                Member.builder().id(1L).build(),
                FavoriteGroup.builder().id(4L).build(),
                Restaurant.builder().id(2L).build()
        )).isFalse();
    }

    @Test
    void existsByMemberAndGroupAndRestaurant_RestaurantNotEqual() {
        // given
        favoriteRepository.save(Favorite.builder()
                .member(Member.builder().id(1L).build())
                .restaurant(Restaurant.builder().id(2L).build())
                .group(FavoriteGroup.builder().id(3L).build())
                .build());

        // expected
        assertThat(favoriteRepository.existsByMemberAndGroupAndRestaurant(
                Member.builder().id(1L).build(),
                FavoriteGroup.builder().id(3L).build(),
                Restaurant.builder().id(4L).build()
        )).isFalse();
    }

    @Test
    void findByMemberAndGroupAndRestaurant() {
        Favorite favorite = favoriteRepository.save(Favorite.builder()
                .member(Member.builder().id(1L).build())
                .restaurant(Restaurant.builder().id(2L).build())
                .group(FavoriteGroup.builder().id(3L).build())
                .build());

        // when
        Favorite result = favoriteRepository.findByMemberAndGroupAndRestaurant(
                Member.builder().id(1L).build(),
                FavoriteGroup.builder().id(3L).build(),
                Restaurant.builder().id(2L).build()
        ).orElseThrow();

        // then
        assertThat(result.getId()).isEqualTo(favorite.getId());
    }

    @Test
    void findByMemberAndGroupAndRestaurant_GroupNotEqual() {
        Favorite favorite = favoriteRepository.save(Favorite.builder()
                .member(Member.builder().id(1L).build())
                .restaurant(Restaurant.builder().id(2L).build())
                .group(FavoriteGroup.builder().id(3L).build())
                .build());

        // expected
        assertThat(favoriteRepository.findByMemberAndGroupAndRestaurant(
                Member.builder().id(1L).build(),
                FavoriteGroup.builder().id(4L).build(),
                Restaurant.builder().id(2L).build()
        )).isEmpty();
    }

    @Test
    void findByMemberAndGroupAndRestaurant_RestaurantNotEqual() {
        Favorite favorite = favoriteRepository.save(Favorite.builder()
                .member(Member.builder().id(1L).build())
                .restaurant(Restaurant.builder().id(2L).build())
                .group(FavoriteGroup.builder().id(3L).build())
                .build());

        // expected
        assertThat(favoriteRepository.findByMemberAndGroupAndRestaurant(
                Member.builder().id(1L).build(),
                FavoriteGroup.builder().id(3L).build(),
                Restaurant.builder().id(4L).build()
        )).isEmpty();
    }

    @Test
    void countByMember() {
        // given
        favoriteRepository.saveAll(List.of(
                Favorite.builder()
                        .member(Member.builder().id(1L).build())
                        .restaurant(Restaurant.builder().id(2L).build())
                        .group(FavoriteGroup.builder().id(3L).build())
                        .build(),
                Favorite.builder()
                        .member(Member.builder().id(1L).build())
                        .restaurant(Restaurant.builder().id(4L).build())
                        .group(FavoriteGroup.builder().id(4L).build())
                        .build(),
                Favorite.builder()
                        .member(Member.builder().id(2L).build())
                        .restaurant(Restaurant.builder().id(5L).build())
                        .group(FavoriteGroup.builder().id(3L).build())
                        .build()
        ));

        // when
        long result = favoriteRepository.countByMember(Member.builder().id(1L).build());

        // then
        assertThat(result).isEqualTo(2L);
    }

    @Test
    void existsByMemberAndRestaurant() {
        // given
        favoriteRepository.save(Favorite.builder()
                .member(Member.builder().id(1L).build())
                .restaurant(Restaurant.builder().id(2L).build())
                .build());

        // expected
        assertThat(favoriteRepository.existsByMemberAndRestaurant(
                Member.builder().id(1L).build(),
                Restaurant.builder().id(2L).build()
        )).isTrue();
    }

    @Test
    void existsByMemberAndRestaurant_MemberNotEqual() {
        // given
        favoriteRepository.save(Favorite.builder()
                .member(Member.builder().id(1L).build())
                .restaurant(Restaurant.builder().id(2L).build())
                .build());

        // expected
        assertThat(favoriteRepository.existsByMemberAndRestaurant(
                Member.builder().id(3L).build(),
                Restaurant.builder().id(2L).build()
        )).isFalse();
    }

    @Test
    void existsByMemberAndRestaurant_RestaurantNotEqual() {
        // given
        favoriteRepository.save(Favorite.builder()
                .member(Member.builder().id(1L).build())
                .restaurant(Restaurant.builder().id(2L).build())
                .build());

        // expected
        assertThat(favoriteRepository.existsByMemberAndRestaurant(
                Member.builder().id(1L).build(),
                Restaurant.builder().id(3L).build()
        )).isFalse();
    }

}