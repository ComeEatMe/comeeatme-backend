package com.comeeatme.domain.restaurant;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.restaurant.repository.LocalDataRepository;
import com.comeeatme.domain.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatNoException;

@DataJpaTest
@Transactional
@Import(TestJpaConfig.class)
class LocalDataTest {

    @Autowired
    private LocalDataRepository localDataRepository;
    @Autowired
    private RestaurantRepository restaurantRepository;

    @Test
    @DisplayName("LocalData 생성 및 저장")
    void save() {
        assertThatNoException().isThrownBy(() -> localDataRepository.saveAndFlush(LocalData.builder()
                .restaurant(restaurantRepository.getReferenceById(1L))
                .managementNum("3810000-101-2002-00170")
                .serviceId("07_24_04_P")
                .name("일반음식점")
                .category("경양식")
                .permissionDate("20121213")
                .closedDate("")
                .updateAt(LocalDateTime.of(2022, 11, 27, 17, 19))
                .build()
        ));
    }

}