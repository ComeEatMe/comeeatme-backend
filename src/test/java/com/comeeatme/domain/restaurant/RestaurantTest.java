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
                .kakaoId(25970354L)
                .kakaoPlaceUrl("http://place.map.kakao.com/25970354")
                .name("모노끼 야탑점")
                .categoryName("음식점 > 일식 > 돈까스,우동")
                .phone("031-702-2929")
                .address(Address.builder()
                        .name("경기 성남시 분당구 야탑동 353-4")
                        .roadName("경기 성남시 분당구 야탑로69번길 24-6")
                        .x(127.12729076428592)
                        .y(37.41160407198509)
                        .build())
                .build()));
    }

}