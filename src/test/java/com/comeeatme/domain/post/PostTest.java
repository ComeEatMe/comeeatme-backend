package com.comeeatme.domain.post;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.post.repository.PostRepository;
import com.comeeatme.domain.restaurant.Restaurant;
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
class PostTest {

    @Autowired
    private PostRepository postRepository;

    @Test
    @DisplayName("Post 생성 및 저장")
    void save() {
        assertThatNoException().isThrownBy(() -> postRepository.saveAndFlush(Post.builder()
                .member(Member.builder().id(2L).build())
                .restaurant(Restaurant.builder().id(1L).build())
                .content("test-content")
                .build()
        ));
    }

}