package com.comeeatme.domain.post;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.image.Image;
import com.comeeatme.domain.post.repository.PostImageRepository;
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
class PostImageTest {

    @Autowired
    private PostImageRepository postImageRepository;

    @Test
    @DisplayName("PostImage 생성 및 저장")
    void save() {
        assertThatNoException().isThrownBy(() -> postImageRepository.saveAndFlush(PostImage.builder()
                .post(Post.builder().id(1L).build())
                .image(Image.builder().id(2L).build())
                .build()
        ));
    }

}