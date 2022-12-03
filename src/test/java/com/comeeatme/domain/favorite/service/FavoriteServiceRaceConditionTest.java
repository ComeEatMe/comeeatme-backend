package com.comeeatme.domain.favorite.service;

import com.comeeatme.domain.address.Address;
import com.comeeatme.domain.address.AddressCode;
import com.comeeatme.domain.address.repository.AddressCodeRepository;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.restaurant.Restaurant;
import com.comeeatme.domain.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class FavoriteServiceRaceConditionTest {

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AddressCodeRepository addressCodeRepository;

    @AfterEach
    void afterEach() {
        restaurantRepository.deleteAll();
        memberRepository.deleteAll();
        addressCodeRepository.deleteAll();
    }

    @Test
    void favorite() throws InterruptedException {
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
                        .name("모노끼 야탑점")
                        .phone("")
                        .address(Address.builder()
                                .name("경기 성남시 분당구 야탑동 353-4")
                                .roadName("경기 성남시 분당구 야탑로69번길 24-6")
                                .addressCode(addressCode)
                                .build())
                        .build()
        );

        int memberCount = 10;
        List<Member> members = memberRepository.saveAll(IntStream.range(0, memberCount)
                .mapToObj(i -> Member.builder()
                        .nickname("nickname-" + i)
                        .introduction("")
                        .build()
                ).collect(Collectors.toList())
        );

        // when
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            Member member = members.get(i);
            executorService.submit(() -> {
                try {
                    favoriteService.favorite(restaurant.getId(), member.getId(), null);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then
        Restaurant foundRestaurant = restaurantRepository.findById(restaurant.getId()).orElseThrow();
        assertThat(foundRestaurant.getFavoriteCount()).isEqualTo(10);
    }

    @Test
    void cancelFavorite() {
    }

}