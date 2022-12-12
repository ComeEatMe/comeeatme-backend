package com.comeeatme.domain.like.repository;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.account.repository.AccountRepository;
import com.comeeatme.domain.like.Like;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.post.Post;
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

    @Test
    void findByMemberIdAndPostIds() {
        // given
        List<Like> likes = likeRepository.saveAll(List.of(
                Like.builder()
                        .member(memberRepository.getReferenceById(10L))
                        .post(Post.builder().id(1L).build())
                        .build(),
                Like.builder()
                        .member(memberRepository.getReferenceById(10L))
                        .post(Post.builder().id(2L).build())
                        .build(),
                Like.builder()
                        .member(memberRepository.getReferenceById(11L))
                        .post(Post.builder().id(3L).build())
                        .build()
        ));

        // when
        List<Like> result = likeRepository.findByMemberIdAndPostIds(10L, List.of(1L, 2L, 3L));

        // then
        result.sort((o1, o2) -> (int) (o1.getPost().getId() - o2.getPost().getId()));
        assertThat(result)
                .hasSize(2)
                .extracting("id").containsExactly(likes.get(0).getId(), likes.get(1).getId());
    }

    @Test
    void deleteAllByPost() {
        // given
        List<Like> likes = likeRepository.saveAll(List.of(
                Like.builder()
                        .member(memberRepository.getReferenceById(10L))
                        .post(Post.builder().id(1L).build())
                        .build(),
                Like.builder()
                        .member(memberRepository.getReferenceById(10L))
                        .post(Post.builder().id(2L).build())
                        .build(),
                Like.builder()
                        .member(memberRepository.getReferenceById(11L))
                        .post(Post.builder().id(1L).build())
                        .build()
        ));

        // when
        likeRepository.deleteAllByPost(Post.builder().id(1L).build());

        // then
        List<Like> foundLikes = likeRepository.findAll();
        assertThat(foundLikes)
                .hasSize(1)
                .extracting("id").containsOnly(likes.get(1).getId());
    }

}