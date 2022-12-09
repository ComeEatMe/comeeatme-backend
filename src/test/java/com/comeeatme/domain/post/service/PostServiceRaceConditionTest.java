package com.comeeatme.domain.post.service;

import com.comeeatme.domain.address.Address;
import com.comeeatme.domain.address.AddressCode;
import com.comeeatme.domain.address.repository.AddressCodeRepository;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.post.repository.PostRepository;
import com.comeeatme.domain.post.request.PostCreate;
import com.comeeatme.domain.restaurant.Restaurant;
import com.comeeatme.domain.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
class PostServiceRaceConditionTest {

    @Autowired
    private PostService postService;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AddressCodeRepository addressCodeRepository;

    @AfterEach
    void afterEach() {
        restaurantRepository.deleteAll();
        postRepository.deleteAll();
        memberRepository.deleteAll();
        addressCodeRepository.deleteAll();
    }

    @Test
    void create() throws InterruptedException {
        // given
        Member member = memberRepository.save(
                Member.builder()
                        .nickname("떡볶이")
                        .introduction("")
                        .build()
        );
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

        // when
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    PostCreate postCreate = PostCreate.builder()
                            .restaurantId(restaurant.getId())
                            .hashtags(Collections.emptySet())
                            .imageIds(Collections.emptyList())
                            .content("content")
                            .build();
                    postService.create(postCreate, member.getId());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then
        Restaurant foundRestaurant = restaurantRepository.findById(restaurant.getId()).orElseThrow();
        assertThat(foundRestaurant.getPostCount()).isEqualTo(100);
    }

    @Test
    void delete() throws InterruptedException {
        // given
        Member member = memberRepository.save(
                Member.builder()
                        .nickname("떡볶이")
                        .introduction("")
                        .build()
        );
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

        int postCount = 200;
        List<Long> postIds = new ArrayList<>();
        for (int i = 0; i < postCount; i++) {
            PostCreate postCreate = PostCreate.builder()
                    .restaurantId(restaurant.getId())
                    .hashtags(Collections.emptySet())
                    .imageIds(Collections.emptyList())
                    .content("content")
                    .build();
            Long postId = postService.create(postCreate, member.getId()).getId();
            postIds.add(postId);
        }

        // when
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            Long postId = postIds.get(i);
            executorService.submit(() -> {
                try {
                    postService.delete(postId);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then
        Restaurant foundRestaurant = restaurantRepository.findById(restaurant.getId()).orElseThrow();
        assertThat(foundRestaurant.getPostCount()).isEqualTo(100);
    }

    @Test
    void deleteAllOfMember() throws InterruptedException {
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

        int numMember = 100;
        List<Member> members = memberRepository.saveAll(IntStream.range(0, numMember)
                .mapToObj(i -> Member.builder()
                        .nickname("nickname-" + i)
                        .introduction("")
                        .build()
                ).collect(Collectors.toList())
        );

        int numPostPerMember = 3;
        List<Restaurant> restaurants = restaurantRepository.saveAll(IntStream.range(0, numPostPerMember)
                .mapToObj(i -> Restaurant.builder()
                        .name("restaurant-" + i)
                        .phone("")
                        .address(Address.builder()
                                .name("address-name")
                                .roadName("road-address-name")
                                .addressCode(addressCode)
                                .build())
                        .build()
                ).collect(Collectors.toList())
        );

        for (int i = 0; i < numMember; i++) {
            Member member = members.get(i);
            for (int j = 0; j < numPostPerMember; j++) {
                Restaurant restaurant = restaurants.get(j);
                PostCreate postCreate = PostCreate.builder()
                        .restaurantId(restaurant.getId())
                        .hashtags(Collections.emptySet())
                        .imageIds(Collections.emptyList())
                        .content("content")
                        .build();
                postService.create(postCreate, member.getId());
            }
        }

        // when
        int threadCount = numMember;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            Member member = members.get(i);
            executorService.submit(() -> {
                try {
                    postService.deleteAllOfMember(member.getId());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then
        List<Restaurant> foundRestaurants = restaurantRepository.findAll();
        for (Restaurant foundRestaurant : foundRestaurants) {
            assertThat(foundRestaurant.getPostCount()).isZero();
        }
    }

}