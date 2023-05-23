package com.comeeatme.domain.image.repository;

import com.comeeatme.domain.common.TestJpaConfig;
import com.comeeatme.domain.image.Image;
import com.comeeatme.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
@Import(TestJpaConfig.class)
class ImageRepositoryTest {

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void findAllByMemberAndUseYnIsTrue() {
        // given
        List<Image> images = imageRepository.saveAll(List.of(
                Image.builder()
                        .member(memberRepository.getReferenceById(1L))
                        .originName("origin-name-1")
                        .storedName("stored-name-1")
                        .url("url-1")
                        .build(),
                Image.builder()
                        .member(memberRepository.getReferenceById(1L))
                        .originName("origin-name-2")
                        .storedName("stored-name-2")
                        .url("url-2")
                        .build(),
                Image.builder()
                        .member(memberRepository.getReferenceById(2L))
                        .originName("origin-name-3")
                        .storedName("stored-name-3")
                        .url("url-3")
                        .build()
        ));
        images.get(1).delete();

        // when
        List<Image> result = imageRepository.findAllByMemberAndUseYnIsTrue(memberRepository.getReferenceById(1L));

        // then
        assertThat(result)
                .hasSize(1)
                .extracting("id").containsOnly(images.get(0).getId());
    }
}