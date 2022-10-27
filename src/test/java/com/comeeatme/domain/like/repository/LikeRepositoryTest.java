package com.comeeatme.domain.like.repository;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.like.Like;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.post.Post;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
@Import(TestJpaConfig.class)
class LikeRepositoryTest {

    @Autowired
    private LikeRepository likesRepository;

    @Test
    void findByPostAndMember_Present() {
        // given
        Like like = likesRepository.saveAndFlush(
                Like.builder()
                        .post(Post.builder().id(1L).build())
                        .member(Member.builder().id(2L).build())
                        .build());

        // when
        Optional<Like> foundLike = likesRepository.findByPostAndMember(
                Post.builder().id(1L).build(), Member.builder().id(2L).build());

        // then
        assertThat(foundLike).isPresent();
    }

    @Test
    void findByPostAndMember_Empty() {
        // given
        likesRepository.saveAllAndFlush(List.of(
                Like.builder() // Not equal post id
                        .post(Post.builder().id(2L).build())
                        .member(Member.builder().id(2L).build())
                        .build(),
                Like.builder() // Not equal member id
                        .post(Post.builder().id(1L).build())
                        .member(Member.builder().id(3L).build())
                        .build()
        ));

        // when
        Optional<Like> foundLike = likesRepository.findByPostAndMember(
                Post.builder().id(1L).build(), Member.builder().id(2L).build());

        // then
        assertThat(foundLike).isEmpty();
    }

    @Test
    void countByPost() {
        // given
        likesRepository.saveAllAndFlush(List.of(
                Like.builder()
                        .post(Post.builder().id(2L).build())
                        .member(Member.builder().id(2L).build())
                        .build(),
                Like.builder()
                        .post(Post.builder().id(2L).build())
                        .member(Member.builder().id(3L).build())
                        .build(),
                Like.builder() // 다른 Post ID -> count 에 포함 X
                        .post(Post.builder().id(1L).build())
                        .member(Member.builder().id(3L).build())
                        .build()
        ));

        // when
        Long count = likesRepository.countByPost(Post.builder().id(2L).build());

        // then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void existsByPostAndMember() {
        // given
        likesRepository.save(Like.builder()
                .post(Post.builder().id(1L).build())
                .member(Member.builder().id(2L).build())
                .build());

        // when
        boolean result = likesRepository.existsByPostAndMember(
                Post.builder().id(1L).build(),
                Member.builder().id(2L).build()
        );

        // then
        assertThat(result).isTrue();
    }

    @Test
    void existsByPostAndMember_PostNotEqual() {
        // given
        likesRepository.save(Like.builder()
                .post(Post.builder().id(1L).build())
                .member(Member.builder().id(2L).build())
                .build());

        // when
        boolean result = likesRepository.existsByPostAndMember(
                Post.builder().id(2L).build(),
                Member.builder().id(2L).build()
        );

        // then
        assertThat(result).isFalse();
    }

    @Test
    void existsByPostAndMember_MemberNotEqual() {
        // given
        likesRepository.save(Like.builder()
                .post(Post.builder().id(1L).build())
                .member(Member.builder().id(2L).build())
                .build());

        // when
        boolean result = likesRepository.existsByPostAndMember(
                Post.builder().id(1L).build(),
                Member.builder().id(1L).build()
        );

        // then
        assertThat(result).isFalse();
    }

}