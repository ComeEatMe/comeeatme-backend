package com.comeeatme.domain.post;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.post.repository.PostRepository;
import com.comeeatme.domain.restaurant.Restaurant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
@Import(TestJpaConfig.class)
class PostHashtagTest {

    @Autowired
    private PostRepository postRepository;

    @Test
    void createAndSave() {
        // given
        Post post = postRepository.save(Post.builder()
                .member(Member.builder().id(2L).build())
                .restaurant(Restaurant.builder().id(1L).build())
                .content("test-content")
                .build()
        );
        post.addHashtag(Hashtag.DATE);
        post.addHashtag(Hashtag.DATE);
        post.addHashtag(Hashtag.COST_EFFECTIVENESS);

        // when
        Post foundPost = postRepository.findById(post.getId()).orElseThrow();

        // then
        assertThat(foundPost.getPostHashtags())
                .hasSize(2)
                .extracting("hashtag").containsOnly(Hashtag.DATE, Hashtag.COST_EFFECTIVENESS);
        assertThat(foundPost.getHashtags())
                .hasSize(2)
                .containsOnly(Hashtag.DATE, Hashtag.COST_EFFECTIVENESS);
    }
}