package com.comeeatme.domain.restaurant.repository;

import com.comeeatme.common.TestJpaConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@Transactional
@Import(TestJpaConfig.class)
class RestaurantRepositoryCustomTest {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Test
    void findSliceBySearchAndUseYnIsTrue() {
    }

}