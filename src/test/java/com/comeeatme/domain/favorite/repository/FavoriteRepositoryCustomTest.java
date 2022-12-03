package com.comeeatme.domain.favorite.repository;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.address.Address;
import com.comeeatme.domain.address.AddressCode;
import com.comeeatme.domain.address.repository.AddressCodeRepository;
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

    @Autowired
    private AddressCodeRepository addressCodeRepository;

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
        AddressCode addressCode = addressCodeRepository.saveAll(List.of(
                AddressCode.builder()
                        .code("4100000000")
                        .name("경기도")
                        .fullName("경기도")
                        .depth(1)
                        .terminal(false)
                        .build(),
                AddressCode.builder()
                        .code("4113500000")
                        .name("경기도 성남시 분당구")
                        .fullName("성남시 분당구")
                        .depth(2)
                        .terminal(false)
                        .build(),
                AddressCode.builder()
                        .code("1121510700")
                        .name("경기도 성남시 분당구 야탑동")
                        .fullName("야탑동")
                        .depth(3)
                        .terminal(true)
                        .build()
        )).get(2);

        Restaurant restaurant1 = restaurantRepository.save(Restaurant.builder()
                .name("restaurant-1")
                .phone("031-0000-0000")
                .address(Address.builder()
                        .name("addressName")
                        .roadName("addressRoadName")
                        .addressCode(addressCode)
                        .build())
                .build());

        Restaurant restaurant2 = restaurantRepository.save( Restaurant.builder()
                .name("restaurant-2")
                .phone("031-0000-0000")
                .address(Address.builder()
                        .name("addressName")
                        .roadName("addressRoadName")
                        .addressCode(addressCode)
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

}