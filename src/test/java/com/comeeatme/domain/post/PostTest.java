package com.comeeatme.domain.post;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.post.repository.PostRepository;
import com.comeeatme.domain.restaurant.Restaurant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

@DataJpaTest
@Transactional
@Import(TestJpaConfig.class)
class PostTest {

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("Post 생성 및 저장")
    void save() {
        assertThatNoException().isThrownBy(() -> postRepository.saveAndFlush(Post.builder()
                .member(memberRepository.getReferenceById(2L))
                .restaurant(Restaurant.builder().id(1L).build())
                .content("test-content")
                .build()
        ));
    }

    @Test
    void increaseCommentCount() {
        // given
        Post post = Post.builder().build();

        // when
        post.increaseCommentCount();

        // then
        assertThat(post.getCommentCount()).isEqualTo(1);
    }

    @Test
    void decreaseCommentCount() {
        // given
        Post post = Post.builder().build();

        // when
        post.increaseCommentCount();
        post.increaseCommentCount();
        post.decreaseCommentCount();

        // then
        assertThat(post.getCommentCount()).isEqualTo(1);
    }

    @Test
    void increaseLikeCount() {
        // given
        Post post = Post.builder().build();

        // when
        post.increaseLikeCount();

        // then
        assertThat(post.getLikeCount()).isEqualTo(1);
    }

    @Test
    void decreaseLikeCount() {
        // given
        Post post = Post.builder().build();

        // when
        post.increaseLikeCount();
        post.increaseLikeCount();
        post.decreaseLikeCount();

        // then
        assertThat(post.getLikeCount()).isEqualTo(1);
    }

    @Test
    void increaseBookmarkCount() {
        // given
        Post post = Post.builder().build();

        // when
        post.increaseBookmarkCount();

        // then
        assertThat(post.getBookmarkCount()).isEqualTo(1);
    }

    @Test
    void decreaseBookmarkCount() {
        // given
        Post post = Post.builder().build();

        // when
        post.increaseBookmarkCount();
        post.increaseBookmarkCount();
        post.decreaseBookmarkCount();

        // then
        assertThat(post.getBookmarkCount()).isEqualTo(1);
    }


}