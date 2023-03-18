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
import org.springframework.data.domain.Sort;
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

    @Test
    void findWithPessimisticLockById() throws InterruptedException {
        // given
        AddressCode addressCode = addressCodeRepository.save(
                AddressCode.builder()
                        .code("1121510700")
                        .name("경기도 성남시 분당구 야탑동")
                        .fullName("야탑동")
                        .depth(3)
                        .terminal(true)
                        .build()
        );

        Restaurant restaurant = restaurantRepository.save(
                Restaurant.builder()
                        .name("테스트 야탑점")
                        .phone("031-000-0000")
                        .address(Address.builder()
                                .name("경기도 성남시 분당구 야탑동")
                                .roadName("경기도 성남시 분당구 야탑로")
                                .addressCode(addressCode)
                                .build())
                        .build()
        );

        assertThat(restaurantRepository.findWithPessimisticLockById(restaurant.getId())).isPresent();
    }

    @Test
    void findSliceByPostCountGreaterThanAndUseYnIsTrue() {
        // given
        AddressCode addressCode = addressCodeRepository.save(
                AddressCode.builder()
                        .code("4113510700")
                        .name("경기도 성남시 분당구 야탑동")
                        .fullName("야탑동")
                        .depth(3)
                        .terminal(true)
                        .build()
        );

        List<Restaurant> restaurants = restaurantRepository.saveAll(List.of(
                Restaurant.builder()
                        .name("음식점1")
                        .phone("031-000-0000")
                        .address(Address.builder()
                                .name("경기도 성남시 분당구 야탑동")
                                .roadName("경기도 성남시 분당구 야탑로")
                                .addressCode(addressCode)
                                .build())
                        .build(),
                Restaurant.builder()
                        .name("음식점2")
                        .phone("031-000-0000")
                        .address(Address.builder()
                                .name("경기도 성남시 분당구 야탑동")
                                .roadName("경기도 성남시 분당구 야탑로")
                                .addressCode(addressCode)
                                .build())
                        .build(),
                Restaurant.builder()
                        .name("음식점3")
                        .phone("031-000-0000")
                        .address(Address.builder()
                                .name("경기도 성남시 분당구 야탑동")
                                .roadName("경기도 성남시 분당구 야탑로")
                                .addressCode(addressCode)
                                .build())
                        .build()
        ));

        restaurants.get(0).increasePostCount();
        restaurants.get(0).increasePostCount();

        restaurants.get(1).increasePostCount();
        restaurants.get(1).increasePostCount();
        restaurants.get(1).increasePostCount();

        // when
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.Direction.DESC, "postCount");
        Slice<Restaurant> result = restaurantRepository.findSliceByPostCountGreaterThanAndUseYnIsTrue(pageRequest, 0);

        // then
        List<Restaurant> content = result.getContent();
        assertThat(content)
                .hasSize(2)
                .extracting("id").containsOnly(
                        restaurants.get(1).getId(),
                        restaurants.get(0).getId()
                );
    }

    @Test
    void findSliceByAddressAddressCodeCodeStartingWithAndPostCountGreaterThanAndUseYnIsTrue() {
        // given
        AddressCode addressCode1 = addressCodeRepository.save(
                AddressCode.builder()
                        .code("4113510700")
                        .name("경기도 성남시 분당구 야탑동")
                        .fullName("야탑동")
                        .depth(3)
                        .terminal(true)
                        .build()
        );

        AddressCode addressCode2 = addressCodeRepository.save(
                AddressCode.builder()
                        .code("1121510700")
                        .name("서울특별시 광진구 화양동")
                        .fullName("화양동")
                        .depth(3)
                        .terminal(true)
                        .build()
        );

        List<Restaurant> restaurants = restaurantRepository.saveAll(List.of(
                Restaurant.builder()
                        .name("음식점1")
                        .phone("031-000-0000")
                        .address(Address.builder()
                                .name("경기도 성남시 분당구 야탑동")
                                .roadName("경기도 성남시 분당구 야탑로")
                                .addressCode(addressCode1)
                                .build())
                        .build(),
                Restaurant.builder()
                        .name("음식점2")
                        .phone("031-000-0000")
                        .address(Address.builder()
                                .name("경기도 성남시 분당구 야탑동")
                                .roadName("경기도 성남시 분당구 야탑로")
                                .addressCode(addressCode1)
                                .build())
                        .build(),
                Restaurant.builder()
                        .name("음식점3")
                        .phone("031-000-0000")
                        .address(Address.builder()
                                .name("경기도 성남시 분당구 야탑동")
                                .roadName("경기도 성남시 분당구 야탑로")
                                .addressCode(addressCode1)
                                .build())
                        .build(),
                Restaurant.builder()
                        .name("음식점4")
                        .phone("02-000-0000")
                        .address(Address.builder()
                                .name("서울특별시 광진구 화양동")
                                .roadName("서울특별시 광진구 능동로")
                                .addressCode(addressCode2)
                                .build())
                        .build()
        ));


        restaurants.get(0).increasePostCount();
        restaurants.get(0).increasePostCount();

        restaurants.get(1).increasePostCount();
        restaurants.get(1).increasePostCount();
        restaurants.get(1).increasePostCount();

        restaurants.get(3).increasePostCount();

        // when
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.Direction.DESC, "postCount");
        Slice<Restaurant> result = restaurantRepository
                .findSliceByAddressAddressCodeCodeStartingWithAndPostCountGreaterThanAndUseYnIsTrue(
                        pageRequest, "41135", 0
                );

        // then
        List<Restaurant> content = result.getContent();
        assertThat(content)
                .hasSize(2)
                .extracting("id").containsOnly(
                        restaurants.get(1).getId(),
                        restaurants.get(0).getId()
                );
    }

    @Test
    void findAllWithPessimisticLockByIdIn() {
        // given
        AddressCode addressCode = addressCodeRepository.save(
                AddressCode.builder()
                        .code("4113510700")
                        .name("경기도 성남시 분당구 야탑동")
                        .fullName("야탑동")
                        .depth(3)
                        .terminal(true)
                        .build()
        );

        List<Restaurant> restaurants = restaurantRepository.saveAll(List.of(
                Restaurant.builder()
                        .name("음식점1")
                        .phone("031-000-0000")
                        .address(Address.builder()
                                .name("경기도 성남시 분당구 야탑동")
                                .roadName("경기도 성남시 분당구 야탑로")
                                .addressCode(addressCode)
                                .build())
                        .build(),
                Restaurant.builder()
                        .name("음식점2")
                        .phone("031-000-0000")
                        .address(Address.builder()
                                .name("경기도 성남시 분당구 야탑동")
                                .roadName("경기도 성남시 분당구 야탑로")
                                .addressCode(addressCode)
                                .build())
                        .build(),
                Restaurant.builder()
                        .name("음식점3")
                        .phone("031-000-0000")
                        .address(Address.builder()
                                .name("경기도 성남시 분당구 야탑동")
                                .roadName("경기도 성남시 분당구 야탑로")
                                .addressCode(addressCode)
                                .build())
                        .build()
        ));

        // when
        List<Restaurant> result = restaurantRepository.findAllWithPessimisticLockByIdIn(
                List.of(restaurants.get(0).getId(), restaurants.get(1).getId()));

        // then
        assertThat(result)
                .hasSize(2)
                .extracting("id").containsOnly(restaurants.get(0).getId(), restaurants.get(1).getId());
    }

}