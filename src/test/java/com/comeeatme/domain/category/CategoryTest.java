package com.comeeatme.domain.category;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.category.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Transactional
@Import(TestJpaConfig.class)
class CategoryTest {

    @Autowired
    CategoryRepository categoryRepository;

    @Test
    @DisplayName("RestaurantCategory 생성 및 저장")
    void createAndSave() {
        assertThatNoException()
                .isThrownBy(() -> categoryRepository.saveAndFlush(Category.builder()
                        .name("음식점")
                        .fullName("음식점")
                        .depth(1)
                        .build()));
    }

    @Test
    @DisplayName("RestaurantCategory depth, parent 유니크")
    void depthParentUnique() {
        categoryRepository.save(Category.builder()
                .name("음식점")
                .fullName("음식점")
                .depth(1)
                .build());
        assertThatThrownBy(() -> categoryRepository.save(Category.builder()
                .name("음식점")
                .fullName("음식점")
                .depth(1)
                .build()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}