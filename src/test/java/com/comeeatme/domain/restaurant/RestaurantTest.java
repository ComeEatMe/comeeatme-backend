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
                .name("모노끼 야탑점")
                .phone("031-702-2929")
                .address(Address.builder()
                        .name("경기 성남시 분당구 야탑동 353-4")
                        .roadName("경기 성남시 분당구 야탑로69번길 24-6")
                        .x(211199.96154825)
                        .y(434395.793544651)
                        .build())
                .build()));
    }

}