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
class RestaurantRepositoryCustomTest {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private AddressCodeRepository addressCodeRepository;

    @Test
    void findSliceByNameAndAddressCodesAndUseYnIsTrue_AddressCodeNull() {
        // given
        AddressCode yatapCode = addressCodeRepository.save(AddressCode.builder()
                .code("4113510700")
                .name("경기도 성남시 분당구 야탑동")
                .fullName("야탑동")
                .depth(3)
                .terminal(true)
                .build());

        AddressCode hwayangCode = addressCodeRepository.save(AddressCode.builder()
                .code("1121510700")
                .name("서울특별시 광진구 화양동")
                .fullName("화양동")
                .depth(3)
                .terminal(true)
                .build());

        restaurantRepository.saveAll(List.of(
                Restaurant.builder()
                        .name("모노끼 야탑점")
                        .phone("")
                        .address(Address.builder()
                                .name("야탑동")
                                .roadName("야탑로69번길")
                                .addressCode(yatapCode)
                                .build())
                        .build(),
                Restaurant.builder()
                        .name("모노끼 화양점")
                        .phone("")
                        .address(Address.builder()
                                .name("화양동")
                                .roadName("능동로")
                                .addressCode(hwayangCode)
                                .build())
                        .build(),
                Restaurant.builder()
                        .name("지그재그")
                        .phone("")
                        .address(Address.builder()
                                .name("화양동")
                                .roadName("능동로")
                                .addressCode(hwayangCode)
                                .build())
                        .build()
        ));

        // when
        PageRequest pageRequest = PageRequest.of(0, 10);
        Slice<Restaurant> result = restaurantRepository.findSliceByNameAndAddressCodesAndUseYnIsTrue(
                pageRequest, "모노끼", null);

        // then
        List<Restaurant> content = result.getContent();
        assertThat(content)
                .hasSize(2)
                .extracting("name").containsOnly("모노끼 야탑점", "모노끼 화양점");
    }

    @Test
    void findSliceByNameAndAddressCodesAndUseYnIsTrue() {
        // given
        AddressCode yatapCode = addressCodeRepository.save(AddressCode.builder()
                .code("4113510700")
                .name("경기도 성남시 분당구 야탑동")
                .fullName("야탑동")
                .depth(3)
                .terminal(true)
                .build());

        AddressCode hwayangCode = addressCodeRepository.save(AddressCode.builder()
                .code("1121510700")
                .name("서울특별시 광진구 화양동")
                .fullName("화양동")
                .depth(3)
                .terminal(true)
                .build());

        restaurantRepository.saveAll(List.of(
                Restaurant.builder()
                        .name("모노끼 야탑점")
                        .phone("")
                        .address(Address.builder()
                                .name("야탑동")
                                .roadName("야탑로69번길")
                                .addressCode(yatapCode)
                                .build())
                        .build(),
                Restaurant.builder()
                        .name("모노끼 화양점")
                        .phone("")
                        .address(Address.builder()
                                .name("화양동")
                                .roadName("능동로")
                                .addressCode(hwayangCode)
                                .build())
                        .build(),
                Restaurant.builder()
                        .name("지그재그")
                        .phone("")
                        .address(Address.builder()
                                .name("화양동")
                                .roadName("능동로")
                                .addressCode(hwayangCode)
                                .build())
                        .build()
        ));

        // when
        PageRequest pageRequest = PageRequest.of(0, 10);
        Slice<Restaurant> result = restaurantRepository.findSliceByNameAndAddressCodesAndUseYnIsTrue(
                pageRequest, "모노끼", List.of(yatapCode));

        // then
        List<Restaurant> content = result.getContent();
        assertThat(content)
                .hasSize(1)
                .extracting("name").containsOnly("모노끼 야탑점");
    }

    @Test
    void findSliceByNameAddressCodesStartingWithAndUseYnIsTrue() {
        // given
        List<AddressCode> addressCodes = addressCodeRepository.saveAll(List.of(
                AddressCode.builder()
                        .code("1100000000")
                        .name("서울특별시")
                        .fullName("서울특별시")
                        .depth(1)
                        .terminal(false)
                        .build(),
                AddressCode.builder()
                        .code("1121500000")
                        .name("서울특별시 광진구")
                        .fullName("광진구")
                        .depth(2)
                        .terminal(false)
                        .build(),
                AddressCode.builder()
                        .code("1121510700")
                        .name("서울특별시 광진구 화양동")
                        .fullName("화양동")
                        .depth(3)
                        .terminal(true)
                        .build()
        ));

        restaurantRepository.save(Restaurant.builder()
                .name("지그재그")
                .phone("")
                .address(Address.builder()
                        .name("화양동")
                        .roadName("능동로")
                        .addressCode(addressCodes.get(2))
                        .build())
                .build());

        // when
        PageRequest pageRequest = PageRequest.of(0, 10);
        Slice<Restaurant> result = restaurantRepository.findSliceByNameAddressCodesStartingWithAndUseYnIsTrue(
                pageRequest, "지그재그", List.of("11215"));

        // then
        List<Restaurant> content = result.getContent();
        assertThat(content)
                .hasSize(1)
                .extracting("name").containsOnly("지그재그");
    }

}