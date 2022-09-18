package com.comeeatme.domain.restaurant;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThatNoException;

@DataJpaTest
@Transactional
@Import(TestJpaConfig.class)
class RestaurantTest {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Test
    @DisplayName("Restaurant 생성 및 저장")
    void save() {
        assertThatNoException().isThrownBy(() -> restaurantRepository.saveAndFlush(Restaurant.builder()
                .kakaoId(2L)
                .kakaoPlaceUrl("test-url")
                .name("맛있는 음식점")
                .categoryGroup("일식")
                .category("돈까스,우동")
                .phone("031-000-0000")
                .address(Address.builder()
                        .name("경기도 성남시 분당구 야탑동 땡땡땡땡")
                        .roadName("경기 성남시 분당구 야탑로땡땡번길 땡땡땡땡")
                        .x(1D)
                        .y(1D)
                        .build())
                .build()));
    }

}