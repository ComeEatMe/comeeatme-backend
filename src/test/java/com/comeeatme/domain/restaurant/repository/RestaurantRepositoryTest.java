package com.comeeatme.domain.restaurant.repository;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.address.Address;
import com.comeeatme.domain.restaurant.Restaurant;
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
class RestaurantRepositoryTest {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Test
    void findSliceByNameStartingWithAndUseYnIsTrue() {
        // given
        restaurantRepository.saveAllAndFlush(List.of(
                Restaurant.builder()
                        .name("테스트 야탑점")
                        .phone("031-000-0000")
                        .address(Address.builder()
                                .name("경기도 성남시 분당구 야탑동")
                                .roadName("경기도 성남시 분당구 야탑로")
                                .x(0D)
                                .y(0D)
                                .build())
                        .build(),
                Restaurant.builder()
                        .name("테스트 화양점")
                        .phone("02-000-0000")
                        .address(Address.builder()
                                .name("서울특별시 광진구 화양동")
                                .roadName("서울특별시 광진구 군자로")
                                .x(0D)
                                .y(0D)
                                .build())
                        .build(),
                Restaurant.builder()
                        .name("Test")
                        .phone("031-000-0000")
                        .address(Address.builder()
                                .name("경기도 성남시 분당구 야탑동")
                                .roadName("경기도 성남시 분당구 야탑로")
                                .x(0D)
                                .y(0D)
                                .build())
                        .build()
        ));
        PageRequest pageRequest = PageRequest.of(0, 10);

        // when
        Slice<Restaurant> restaurantSlice = restaurantRepository.findSliceByNameStartingWithAndUseYnIsTrue(pageRequest, "테스트");

        // then
        assertThat(restaurantSlice.getContent())
                .hasSize(2)
                .extracting("name")
                .containsExactly("테스트 야탑점", "테스트 화양점");
    }
}