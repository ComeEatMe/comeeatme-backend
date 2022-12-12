package com.comeeatme.domain.favorite;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.favorite.repository.FavoriteRepository;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Transactional
@Import(TestJpaConfig.class)
class FavoriteTest {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Test
    void save() {
        assertThatNoException().isThrownBy(() -> favoriteRepository.save(
                Favorite.builder()
                        .member(memberRepository.getReferenceById(1L))
                        .restaurant(restaurantRepository.getReferenceById(2L))
                        .build()
        ));
    }

    @Test
    void unique_Member_Restaurant() {
        assertThatThrownBy(() -> favoriteRepository.saveAll(List.of(
                Favorite.builder()
                        .member(memberRepository.getReferenceById(1L))
                        .restaurant(restaurantRepository.getReferenceById(2L))
                        .build(),
                Favorite.builder()
                        .member(memberRepository.getReferenceById(1L))
                        .restaurant(restaurantRepository.getReferenceById(2L))
                        .build()
                )
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

}