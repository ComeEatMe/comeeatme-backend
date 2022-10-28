package com.comeeatme.domain.like.repository;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.account.repository.AccountRepository;
import com.comeeatme.domain.like.Like;
import com.comeeatme.domain.like.response.LikeCount;
import com.comeeatme.domain.member.Member;
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
    void countsGroupByPosts() {
        // given
        likeRepository.saveAll(List.of(
                Like.builder()
                        .post(Post.builder().id(1L).build())
                        .member(Member.builder().id(1L).build())
                        .build(),
                Like.builder()
                        .post(Post.builder().id(1L).build())
                        .member(Member.builder().id(2L).build())
                        .build(),
                Like.builder() // 다른 Post ID -> count 에 포함 X
                        .post(Post.builder().id(1L).build())
                        .member(Member.builder().id(3L).build())
                        .build(),
                Like.builder()
                        .post(Post.builder().id(2L).build())
                        .member(Member.builder().id(1L).build())
                        .build(),
                Like.builder()
                        .post(Post.builder().id(2L).build())
                        .member(Member.builder().id(2L).build())
                        .build(),
                Like.builder() // 다른 Post ID -> count 에 포함 X
                        .post(Post.builder().id(3L).build())
                        .member(Member.builder().id(1L).build())
                        .build()
        ));

        // when
        List<LikeCount> counts = likeRepository.countsGroupByPosts(List.of(
                Post.builder().id(1L).build(),
                Post.builder().id(2L).build()
        ));

        // then
        counts.sort((o1, o2) -> (int) (o1.getPostId() - o2.getPostId()));
        assertThat(counts).extracting("postId").containsExactly(1L, 2L);
        assertThat(counts).extracting("count").containsExactly(3L, 2L);
    }

    @Test
    void findByMemberIdAndPostIds() {
        // given
        List<Like> likes = likeRepository.saveAll(List.of(
                Like.builder()
                        .member(Member.builder().id(10L).build())
                        .post(Post.builder().id(1L).build())
                        .build(),
                Like.builder()
                        .member(Member.builder().id(10L).build())
                        .post(Post.builder().id(2L).build())
                        .build(),
                Like.builder()
                        .member(Member.builder().id(11L).build())
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

}