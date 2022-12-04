package com.comeeatme.domain.like.repository;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.like.Like;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.post.Post;
import com.comeeatme.domain.post.repository.PostRepository;
import com.comeeatme.domain.restaurant.Restaurant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceUnitUtil;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
@Import(TestJpaConfig.class)
class LikeRepositoryTest {

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private EntityManager em;

    @Test
    void findByPostAndMember_Present() {
        // given
        Like like = likeRepository.saveAndFlush(
                Like.builder()
                        .post(Post.builder().id(1L).build())
                        .member(Member.builder().id(2L).build())
                        .build());

        // when
        Optional<Like> foundLike = likeRepository.findByPostAndMember(
                Post.builder().id(1L).build(), Member.builder().id(2L).build());

        // then
        assertThat(foundLike).isPresent();
    }

    @Test
    void findByPostAndMember_Empty() {
        // given
        likeRepository.saveAllAndFlush(List.of(
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
        Optional<Like> foundLike = likeRepository.findByPostAndMember(
                Post.builder().id(1L).build(), Member.builder().id(2L).build());

        // then
        assertThat(foundLike).isEmpty();
    }

    @Test
    void existsByPostAndMember() {
        // given
        likeRepository.save(Like.builder()
                .post(Post.builder().id(1L).build())
                .member(Member.builder().id(2L).build())
                .build());

        // when
        boolean result = likeRepository.existsByPostAndMember(
                Post.builder().id(1L).build(),
                Member.builder().id(2L).build()
        );

        // then
        assertThat(result).isTrue();
    }

    @Test
    void existsByPostAndMember_PostNotEqual() {
        // given
        likeRepository.save(Like.builder()
                .post(Post.builder().id(1L).build())
                .member(Member.builder().id(2L).build())
                .build());

        // when
        boolean result = likeRepository.existsByPostAndMember(
                Post.builder().id(2L).build(),
                Member.builder().id(2L).build()
        );

        // then
        assertThat(result).isFalse();
    }

    @Test
    void existsByPostAndMember_MemberNotEqual() {
        // given
        likeRepository.save(Like.builder()
                .post(Post.builder().id(1L).build())
                .member(Member.builder().id(2L).build())
                .build());

        // when
        boolean result = likeRepository.existsByPostAndMember(
                Post.builder().id(1L).build(),
                Member.builder().id(1L).build()
        );

        // then
        assertThat(result).isFalse();
    }

    @Test
    void findSliceWithPostByMember() {
        // given
        Post post = postRepository.save(Post.builder()
                .content("content")
                .restaurant(Restaurant.builder().id(10L).build())
                .member(Member.builder().id(20L).build())
                .build());

        List<Like> likes = likeRepository.saveAll(List.of(
                Like.builder()
                        .post(post)
                        .member(Member.builder().id(21L).build())
                        .build(),
                Like.builder()
                        .post(post)
                        .member(Member.builder().id(22L).build())
                        .build()
        ));

        em.flush();
        em.clear();

        // when
        PageRequest pageRequest = PageRequest.of(0, 10);
        Slice<Like> result = likeRepository.findSliceWithPostByMember(
                pageRequest, Member.builder().id(21L).build());


        // then
        List<Like> content = result.getContent();
        assertThat(content)
                .hasSize(1)
                .extracting("id").containsExactly(likes.get(0).getId());

        PersistenceUnitUtil persistenceUnitUtil = em.getEntityManagerFactory().getPersistenceUnitUtil();
        for (Like foundLike : content) {
            assertThat(persistenceUnitUtil.isLoaded(foundLike.getPost())).isTrue();
        }

    }

}