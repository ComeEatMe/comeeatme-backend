package com.comeeatme.domain.restaurant;

import com.comeeatme.domain.address.Address;
import com.comeeatme.domain.address.AddressCode;
import com.comeeatme.domain.address.repository.AddressCodeRepository;
import com.comeeatme.domain.common.TestJpaConfig;
import com.comeeatme.domain.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Transactional
@Import(TestJpaConfig.class)
class RestaurantTest {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private AddressCodeRepository addressCodeRepository;

    @Test
    @DisplayName("Restaurant 생성 및 저장")
    void save() {
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
        assertThatNoException().isThrownBy(() -> restaurantRepository.saveAndFlush(Restaurant.builder()
                .name("모노끼 야탑점")
                .phone("031-702-2929")
                .address(Address.builder()
                        .name("경기 성남시 분당구 야탑동 353-4")
                        .roadName("경기 성남시 분당구 야탑로69번길 24-6")
                        .addressCode(addressCode)
                        .build())
                .build()));
    }

    @Test
    void increasePostCount() {
        // given
        Restaurant restaurant = Restaurant.builder().build();

        // when
        restaurant.increasePostCount();

        // then
        assertThat(restaurant.getPostCount()).isEqualTo(1);
    }

    @Test
    void decreasePostCount() {
        // given
        Restaurant restaurant = Restaurant.builder().build();

        // when
        restaurant.increasePostCount();
        restaurant.decreasePostCount();

        // then
        assertThat(restaurant.getPostCount()).isZero();
    }

    @Test
    void decreasePostCount_Fail() {
        // given
        Restaurant restaurant = Restaurant.builder().build();

        // expected
        assertThatThrownBy(restaurant::decreasePostCount)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void increaseFavoriteCount() {
        // given
        Restaurant restaurant = Restaurant.builder().build();

        // when
        restaurant.increaseFavoriteCount();

        // then
        assertThat(restaurant.getFavoriteCount()).isEqualTo(1);
    }

    @Test
    void decreaseFavoriteCount() {
        // given
        Restaurant restaurant = Restaurant.builder().build();

        // when
        restaurant.increaseFavoriteCount();
        restaurant.decreaseFavoriteCount();

        // then
        assertThat(restaurant.getFavoriteCount()).isZero();
    }

    @Test
    void decreaseFavoriteCount_Fail() {
        // given
        Restaurant restaurant = Restaurant.builder().build();

        // expected
        assertThatThrownBy(restaurant::decreaseFavoriteCount)
                .isInstanceOf(IllegalStateException.class);
    }

}