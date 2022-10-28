package com.comeeatme.domain.favorite;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.favorite.repository.FavoriteRepository;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.restaurant.Restaurant;
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

    @Test
    void save() {
        assertThatNoException().isThrownBy(() -> favoriteRepository.save(
                Favorite.builder()
                        .member(Member.builder().id(1L).build())
                        .restaurant(Restaurant.builder().id(2L).build())
                        .build()
        ));
    }

    @Test
    void unique_FavoriteGroup_Restaurant() {
        assertThatThrownBy(() -> favoriteRepository.saveAll(List.of(
                Favorite.builder()
                        .member(Member.builder().id(1L).build())
                        .restaurant(Restaurant.builder().id(2L).build())
                        .group(FavoriteGroup.builder().id(3L).build())
                        .build(),
                Favorite.builder()
                        .member(Member.builder().id(1L).build())
                        .restaurant(Restaurant.builder().id(2L).build())
                        .group(FavoriteGroup.builder().id(3L).build())
                        .build()
                )
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

}