package com.comeeatme.domain.like.repository;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.account.repository.AccountRepository;
import com.comeeatme.domain.like.Like;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.post.repository.PostRepository;
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
class LikeRepositoryCustomTest {

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PostRepository postRepository;

    @Test
    void deleteAllByPost() {
        // given
        List<Like> likes = likeRepository.saveAll(List.of(
                Like.builder()
                        .member(memberRepository.getReferenceById(10L))
                        .post(postRepository.getReferenceById(1L))
                        .build(),
                Like.builder()
                        .member(memberRepository.getReferenceById(10L))
                        .post(postRepository.getReferenceById(2L))
                        .build(),
                Like.builder()
                        .member(memberRepository.getReferenceById(11L))
                        .post(postRepository.getReferenceById(1L))
                        .build()
        ));

        // when
        likeRepository.deleteAllByPost(postRepository.getReferenceById(1L));

        // then
        List<Like> foundLikes = likeRepository.findAll();
        assertThat(foundLikes)
                .hasSize(1)
                .extracting("id").containsOnly(likes.get(1).getId());
    }

}