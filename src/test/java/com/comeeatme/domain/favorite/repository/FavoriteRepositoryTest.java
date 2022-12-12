package com.comeeatme.domain.favorite.repository;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.address.Address;
import com.comeeatme.domain.address.AddressCode;
import com.comeeatme.domain.address.repository.AddressCodeRepository;
import com.comeeatme.domain.favorite.Favorite;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.restaurant.Restaurant;
import com.comeeatme.domain.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
@Import(TestJpaConfig.class)
class FavoriteRepositoryTest {

    @Autowired
    private FavoriteRepository favoriteRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private RestaurantRepository restaurantRepository;
    @Autowired
    private AddressCodeRepository addressCodeRepository;
    @Autowired
    private EntityManagerFactory emf;

    @Test
    void findByRestaurantAndMember() {
        Favorite favorite = favoriteRepository.save(Favorite.builder()
                .member(memberRepository.getReferenceById(1L))
                .restaurant(restaurantRepository.getReferenceById(2L))
                .build());

        // when
        Favorite result = favoriteRepository.findByRestaurantAndMember(
                restaurantRepository.getReferenceById(2L),
                memberRepository.getReferenceById(1L)
        ).orElseThrow();

        // then
        assertThat(result.getId()).isEqualTo(favorite.getId());
    }

    @Test
    void findByRestaurantAndMember_RestaurantNotEqual() {
        Favorite favorite = favoriteRepository.save(Favorite.builder()
                .member(memberRepository.getReferenceById(1L))
                .restaurant(restaurantRepository.getReferenceById(2L))
                .build());

        // expected
        assertThat(favoriteRepository.findByRestaurantAndMember(
                restaurantRepository.getReferenceById(3L),
                memberRepository.getReferenceById(1L)
                )).isEmpty();
    }

    @Test
    void countByMember() {
        // given
        favoriteRepository.saveAll(List.of(
                Favorite.builder()
                        .member(memberRepository.getReferenceById(1L))
                        .restaurant(restaurantRepository.getReferenceById(2L))
                        .build(),
                Favorite.builder()
                        .member(memberRepository.getReferenceById(1L))
                        .restaurant(restaurantRepository.getReferenceById(4L))
                        .build(),
                Favorite.builder()
                        .member(memberRepository.getReferenceById(2L))
                        .restaurant(restaurantRepository.getReferenceById(5L))
                        .build()
        ));

        // when
        long result = favoriteRepository.countByMember(memberRepository.getReferenceById(1L));

        // then
        assertThat(result).isEqualTo(2L);
    }

    @Test
    void existsByMemberAndRestaurant() {
        // given
        favoriteRepository.save(Favorite.builder()
                .member(memberRepository.getReferenceById(1L))
                .restaurant(restaurantRepository.getReferenceById(2L))
                .build());

        // expected
        assertThat(favoriteRepository.existsByRestaurantAndMember(
                restaurantRepository.getReferenceById(2L),
                memberRepository.getReferenceById(1L)
                )).isTrue();
    }

    @Test
    void existsByMemberAndRestaurant_MemberNotEqual() {
        // given
        favoriteRepository.save(Favorite.builder()
                .member(memberRepository.getReferenceById(1L))
                .restaurant(restaurantRepository.getReferenceById(2L))
                .build());

        // expected
        assertThat(favoriteRepository.existsByRestaurantAndMember(
                restaurantRepository.getReferenceById(2L),
                memberRepository.getReferenceById(3L)
                )).isFalse();
    }

    @Test
    void existsByMemberAndRestaurant_RestaurantNotEqual() {
        // given
        favoriteRepository.save(Favorite.builder()
                .member(memberRepository.getReferenceById(1L))
                .restaurant(restaurantRepository.getReferenceById(2L))
                .build());

        // expected
        assertThat(favoriteRepository.existsByRestaurantAndMember(
                restaurantRepository.getReferenceById(3L),
                memberRepository.getReferenceById(1L)
                )).isFalse();
    }

    @Test
    void findSliceWithRestaurantByMember() {
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
                                .name("경기 성남시 분당구")
                                .roadName("경기 성남시 분당구 야탑로")
                                .addressCode(addressCode)
                                .build())
                        .build()
        );

        List<Favorite> favorites = favoriteRepository.saveAll(List.of(
                Favorite.builder()
                        .member(memberRepository.getReferenceById(10L))
                        .restaurant(restaurant)
                        .build(),
                Favorite.builder()
                        .member(memberRepository.getReferenceById(11L))
                        .restaurant(restaurant)
                        .build()
        ));

        // when
        PageRequest pageRequest = PageRequest.of(0, 10);
        Slice<Favorite> result = favoriteRepository.findSliceWithRestaurantByMember(
                pageRequest, memberRepository.getReferenceById(10L));

        // then
        List<Favorite> content = result.getContent();
        assertThat(content)
                .hasSize(1)
                .extracting("id").containsOnly(favorites.get(0).getId());
        PersistenceUnitUtil persistenceUnitUtil = emf.getPersistenceUnitUtil();
        assertThat(persistenceUnitUtil.isLoaded(content.get(0).getRestaurant())).isTrue();
    }

    @Test
    void findAllByMember() {
        // given
        List<Favorite> favorites = favoriteRepository.saveAll(List.of(
                Favorite.builder()
                        .restaurant(restaurantRepository.getReferenceById(1L))
                        .member(memberRepository.getReferenceById(10L))
                        .build(),
                Favorite.builder()
                        .restaurant(restaurantRepository.getReferenceById(2L))
                        .member(memberRepository.getReferenceById(10L))
                        .build(),
                Favorite.builder()      // memberId different
                        .restaurant(restaurantRepository.getReferenceById(1L))
                        .member(memberRepository.getReferenceById(11L))
                        .build()
        ));

        // when
        List<Favorite> result = favoriteRepository.findAllByMember(memberRepository.getReferenceById(10L));

        // then
        assertThat(result)
                .hasSize(2)
                .extracting("id").containsOnly(favorites.get(0).getId(), favorites.get(1).getId());
    }

}