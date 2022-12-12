package com.comeeatme.domain.like;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.like.repository.LikeRepository;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.post.repository.PostRepository;
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
class LikeTest {

    @Autowired
    private LikeRepository likesRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PostRepository postRepository;

    @Test
    void createAndSave() {
        assertThatNoException().isThrownBy(() -> likesRepository.saveAndFlush(Like.builder()
                .member(memberRepository.getReferenceById(1L))
                .post(postRepository.getReferenceById(2L))
                .build()));
    }

    @Test
    void Unique_Member_Post() {
        assertThatThrownBy(() -> likesRepository.saveAllAndFlush(
                List.of(
                        Like.builder()
                                .member(memberRepository.getReferenceById(1L))
                                .post(postRepository.getReferenceById(2L))
                                .build(),
                        Like.builder()
                                .member(memberRepository.getReferenceById(1L))
                                .post(postRepository.getReferenceById(2L))
                                .build()
                )
        ))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

}