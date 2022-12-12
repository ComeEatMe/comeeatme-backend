package com.comeeatme.domain.like.repository;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.like.Like;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.post.Post;
import com.comeeatme.domain.post.repository.PostRepository;
import com.comeeatme.domain.restaurant.repository.RestaurantRepository;
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
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private RestaurantRepository restaurantRepository;

    @Test
    void findByPostAndMember_Present() {
        // given
        Like like = likeRepository.saveAndFlush(
                Like.builder()
                        .post(postRepository.getReferenceById(1L))
                        .member(memberRepository.getReferenceById(2L))
                        .build());

        // when
        Optional<Like> foundLike = likeRepository.findByPostAndMember(
                postRepository.getReferenceById(1L), memberRepository.getReferenceById(2L));

        // then
        assertThat(foundLike).isPresent();
    }

    @Test
    void findByPostAndMember_Empty() {
        // given
        likeRepository.saveAllAndFlush(List.of(
                Like.builder() // Not equal post id
                        .post(postRepository.getReferenceById(2L))
                        .member(memberRepository.getReferenceById(2L))
                        .build(),
                Like.builder() // Not equal member id
                        .post(postRepository.getReferenceById(1L))
                        .member(memberRepository.getReferenceById(3L))
                        .build()
        ));

        // when
        Optional<Like> foundLike = likeRepository.findByPostAndMember(
                postRepository.getReferenceById(1L), memberRepository.getReferenceById(2L));

        // then
        assertThat(foundLike).isEmpty();
    }

    @Test
    void existsByPostAndMember() {
        // given
        likeRepository.save(Like.builder()
                .post(postRepository.getReferenceById(1L))
                .member(memberRepository.getReferenceById(2L))
                .build());

        // when
        boolean result = likeRepository.existsByPostAndMember(
                postRepository.getReferenceById(1L),
                memberRepository.getReferenceById(2L)
        );

        // then
        assertThat(result).isTrue();
    }

    @Test
    void existsByPostAndMember_PostNotEqual() {
        // given
        likeRepository.save(Like.builder()
                .post(postRepository.getReferenceById(1L))
                .member(memberRepository.getReferenceById(2L))
                .build());

        // when
        boolean result = likeRepository.existsByPostAndMember(
                postRepository.getReferenceById(2L),
                memberRepository.getReferenceById(2L)
        );

        // then
        assertThat(result).isFalse();
    }

    @Test
    void existsByPostAndMember_MemberNotEqual() {
        // given
        likeRepository.save(Like.builder()
                .post(postRepository.getReferenceById(1L))
                .member(memberRepository.getReferenceById(2L))
                .build());

        // when
        boolean result = likeRepository.existsByPostAndMember(
                postRepository.getReferenceById(1L),
                memberRepository.getReferenceById(1L)
        );

        // then
        assertThat(result).isFalse();
    }

    @Test
    void findSliceWithPostByMember() {
        // given
        Post post = postRepository.save(Post.builder()
                .content("content")
                .restaurant(restaurantRepository.getReferenceById(10L))
                .member(memberRepository.getReferenceById(20L))
                .build());

        List<Like> likes = likeRepository.saveAll(List.of(
                Like.builder()
                        .post(post)
                        .member(memberRepository.getReferenceById(21L))
                        .build(),
                Like.builder()
                        .post(post)
                        .member(memberRepository.getReferenceById(22L))
                        .build()
        ));

        em.flush();
        em.clear();

        // when
        PageRequest pageRequest = PageRequest.of(0, 10);
        Slice<Like> result = likeRepository.findSliceWithPostByMember(
                pageRequest, memberRepository.getReferenceById(21L));


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

    @Test
    void findAllByMember() {
        // given
        List<Like> likes = likeRepository.saveAll(List.of(
                Like.builder()
                        .member(memberRepository.getReferenceById(1L))
                        .post(postRepository.getReferenceById(10L))
                        .build(),
                Like.builder()  // postId different
                        .member(memberRepository.getReferenceById(1L))
                        .post(postRepository.getReferenceById(11L))
                        .build(),
                Like.builder()  // memberId different
                        .member(memberRepository.getReferenceById(2L))
                        .post(postRepository.getReferenceById(10L))
                        .build()
        ));

        // when
        List<Like> result = likeRepository.findAllByMember(memberRepository.getReferenceById(1L));

        // then
        assertThat(result)
                .hasSize(2)
                .extracting("id").containsOnly(likes.get(0).getId(), likes.get(1).getId());
    }

}