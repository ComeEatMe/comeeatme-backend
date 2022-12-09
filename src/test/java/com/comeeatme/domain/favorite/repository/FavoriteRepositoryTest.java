package com.comeeatme.domain.favorite.repository;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.favorite.Favorite;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.restaurant.Restaurant;
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
class FavoriteRepositoryTest {

    @Autowired
    private FavoriteRepository favoriteRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private RestaurantRepository restaurantRepository;

    @Test
    void findByRestaurantAndMember() {
        Favorite favorite = favoriteRepository.save(Favorite.builder()
                .member(memberRepository.getReferenceById(1L))
                .restaurant(restaurantRepository.getReferenceById(2L))
                .build());

        // when
        Favorite result = favoriteRepository.findByRestaurantAndMember(
                restaurantRepository.getReferenceById(2L),
                memberRepository.getReferenceById(1L)
        ).orElseThrow();

        // then
        assertThat(result.getId()).isEqualTo(favorite.getId());
    }

    @Test
    void findByRestaurantAndMember_RestaurantNotEqual() {
        Favorite favorite = favoriteRepository.save(Favorite.builder()
                .member(memberRepository.getReferenceById(1L))
                .restaurant(restaurantRepository.getReferenceById(2L))
                .build());

        // expected
        assertThat(favoriteRepository.findByRestaurantAndMember(
                restaurantRepository.getReferenceById(3L),
                memberRepository.getReferenceById(1L)
                )).isEmpty();
    }

    @Test
    void countByMember() {
        // given
        favoriteRepository.saveAll(List.of(
                Favorite.builder()
                        .member(Member.builder().id(1L).build())
                        .restaurant(Restaurant.builder().id(2L).build())
                        .build(),
                Favorite.builder()
                        .member(Member.builder().id(1L).build())
                        .restaurant(Restaurant.builder().id(4L).build())
                        .build(),
                Favorite.builder()
                        .member(Member.builder().id(2L).build())
                        .restaurant(Restaurant.builder().id(5L).build())
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
        assertThat(favoriteRepository.existsByRestaurantAndMember(
                Restaurant.builder().id(2L).build(),
                Member.builder().id(1L).build()
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
        assertThat(favoriteRepository.existsByRestaurantAndMember(
                Restaurant.builder().id(2L).build(),
                Member.builder().id(3L).build()
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
        assertThat(favoriteRepository.existsByRestaurantAndMember(
                Restaurant.builder().id(3L).build(),
                Member.builder().id(1L).build()
                )).isFalse();
    }

}