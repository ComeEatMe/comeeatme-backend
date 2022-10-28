package com.comeeatme.domain.like;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.like.repository.LikeRepository;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.post.Post;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Transactional
@Import(TestJpaConfig.class)
class LikeTest {

    @Autowired
    private LikeRepository likesRepository;

    @Test
    void createAndSave() {
        assertThatNoException().isThrownBy(() -> likesRepository.saveAndFlush(Like.builder()
                .member(Member.builder().id(1L).build())
                .post(Post.builder().id(2L).build())
                .build()));
    }

    @Test
    void Unique_Member_Post() {
        assertThatThrownBy(() -> likesRepository.saveAllAndFlush(
                List.of(
                        Like.builder()
                                .member(Member.builder().id(1L).build())
                                .post(Post.builder().id(2L).build())
                                .build(),
                        Like.builder()
                                .member(Member.builder().id(1L).build())
                                .post(Post.builder().id(2L).build())
                                .build()
                )
        ))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

}