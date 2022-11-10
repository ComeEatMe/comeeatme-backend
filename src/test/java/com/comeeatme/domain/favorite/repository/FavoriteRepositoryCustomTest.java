package com.comeeatme.domain.favorite.repository;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.favorite.response.FavoriteCount;
import com.comeeatme.domain.restaurant.Address;
import com.comeeatme.domain.favorite.Favorite;
import com.comeeatme.domain.favorite.FavoriteGroup;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.restaurant.Restaurant;
import com.comeeatme.domain.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
@Import(TestJpaConfig.class)
class FavoriteRepositoryCustomTest {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

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

    @Test
    void findSliceWithByMemberAndGroup() {
        // given
        Restaurant restaurant1 = restaurantRepository.save(Restaurant.builder()
                .name("restaurant-1")
                .phone("031-0000-0000")
                .address(Address.builder()
                        .name("addressName")
                        .roadName("addressRoadName")
                        .x(1.0)
                        .y(2.0)
                        .build())
                .build());

        Restaurant restaurant2 = restaurantRepository.save( Restaurant.builder()
                .name("restaurant-2")
                .phone("031-0000-0000")
                .address(Address.builder()
                        .name("addressName")
                        .roadName("addressRoadName")
                        .x(1.0)
                        .y(2.0)
                        .build())
                .build());

        Favorite favorite1 = favoriteRepository.save(Favorite.builder()
                .member(Member.builder().id(10L).build())
                .group(FavoriteGroup.builder().id(3L).build())
                .restaurant(restaurant1)
                .build());

        Favorite favorite2 = favoriteRepository.save(Favorite.builder()
                .member(Member.builder().id(10L).build())
                .group(FavoriteGroup.builder().id(3L).build())
                .restaurant(restaurant2)
                .build());

        Favorite favorite3 = favoriteRepository.save(Favorite.builder()
                .member(Member.builder().id(10L).build())
                .group(FavoriteGroup.builder().id(4L).build())
                .restaurant(restaurant1)
                .build());

        // when
        PageRequest pageRequest = PageRequest.of(0, 10);
        Slice<Favorite> result = favoriteRepository.findSliceWithByMemberAndGroup(pageRequest,
                Member.builder().id(10L).build(),
                FavoriteGroup.builder().id(3L).build());

        // then
        List<Favorite> content = result.getContent();
        assertThat(content)
                .hasSize(2)
                .extracting("id").containsOnly(favorite1.getId(), favorite2.getId());
    }

    @Test
    void countsGroupByRestaurants() {
        // given
        favoriteRepository.saveAll(List.of(
                Favorite.builder()
                        .restaurant(Restaurant.builder().id(1L).build())
                        .member(Member.builder().id(11L).build())
                        .build(),
                Favorite.builder()
                        .restaurant(Restaurant.builder().id(1L).build())
                        .member(Member.builder().id(11L).build())
                        .group(FavoriteGroup.builder().id(21L).build())
                        .build(),
                Favorite.builder()
                        .restaurant(Restaurant.builder().id(2L).build())
                        .member(Member.builder().id(11L).build())
                        .build(),
                Favorite.builder()
                        .restaurant(Restaurant.builder().id(1L).build())
                        .member(Member.builder().id(12L).build())
                        .build()
        ));

        // when
        List<FavoriteCount> result = favoriteRepository.countsGroupByRestaurants(List.of(
                Restaurant.builder().id(1L).build()
        ));

        // then
        assertThat(result).hasSize(1);
        assertThat(result).extracting("restaurantId").containsOnly(1L);
        assertThat(result).extracting("count").containsOnly(2L);
    }

}