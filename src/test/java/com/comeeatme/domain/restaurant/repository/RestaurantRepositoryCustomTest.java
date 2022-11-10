package com.comeeatme.domain.restaurant.repository;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.restaurant.request.RestaurantSearch;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@Transactional
@Import(TestJpaConfig.class)
class RestaurantRepositoryCustomTest {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Test
    @Disabled
    void findSliceBySearchAndUseYnIsTrue() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        RestaurantSearch search = RestaurantSearch.builder()
                .name("지그재그")
                .x(37.5469873026613)
                .y(127.07255855087)
                .distance(1000.0)
                .build();
        restaurantRepository.findSliceBySearchAndUseYnIsTrue(pageRequest, search);
    }

}