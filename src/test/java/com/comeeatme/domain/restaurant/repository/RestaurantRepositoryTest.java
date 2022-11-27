package com.comeeatme.domain.restaurant.repository;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.address.Address;
import com.comeeatme.domain.address.AddressCode;
import com.comeeatme.domain.address.repository.AddressCodeRepository;
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

    @Autowired
    private AddressCodeRepository addressCodeRepository;

    @Test
    void findSliceByNameStartingWithAndUseYnIsTrue() {
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
        restaurantRepository.saveAllAndFlush(List.of(
                Restaurant.builder()
                        .name("테스트 야탑점")
                        .phone("031-000-0000")
                        .address(Address.builder()
                                .name("경기도 성남시 분당구 야탑동")
                                .roadName("경기도 성남시 분당구 야탑로")
                                .addressCode(addressCode)
                                .build())
                        .build(),
                Restaurant.builder()
                        .name("테스트 화양점")
                        .phone("02-000-0000")
                        .address(Address.builder()
                                .name("서울특별시 광진구 화양동")
                                .roadName("서울특별시 광진구 군자로")
                                .addressCode(addressCode)
                                .build())
                        .build(),
                Restaurant.builder()
                        .name("Test")
                        .phone("031-000-0000")
                        .address(Address.builder()
                                .name("경기도 성남시 분당구 야탑동")
                                .roadName("경기도 성남시 분당구 야탑로")
                                .addressCode(addressCode)
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