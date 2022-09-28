package com.comeeatme.domain.images;


import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.images.repository.ImagesRepository;
import com.comeeatme.domain.member.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThatNoException;

@DataJpaTest
@Transactional
@Import(TestJpaConfig.class)
class ImagesTest {

    @Autowired
    private ImagesRepository imagesRepository;

    @Test
    @DisplayName("Images 생성 및 저장")
    void createAndSave() {
        assertThatNoException().isThrownBy(() -> imagesRepository.saveAndFlush(Images.builder()
                .member(Member.builder().id(1L).build())
                .originName("test-origin-name")
                .storedName("test-stored-name")
                .url("test-url")
                .build()
        ));
    }
}