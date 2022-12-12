package com.comeeatme.domain.image;


import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.image.repository.ImageRepository;
import com.comeeatme.domain.member.repository.MemberRepository;
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
class ImageTest {

    @Autowired
    private ImageRepository imageRepository;
    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("Images 생성 및 저장")
    void createAndSave() {
        assertThatNoException().isThrownBy(() -> imageRepository.saveAndFlush(Image.builder()
                .member(memberRepository.getReferenceById(1L))
                .originName("test-origin-name")
                .storedName("test-stored-name")
                .url("test-url")
                .build()
        ));
    }
}