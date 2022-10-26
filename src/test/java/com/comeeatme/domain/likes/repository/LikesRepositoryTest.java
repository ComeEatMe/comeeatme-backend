package com.comeeatme.domain.likes.repository;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.account.Account;
import com.comeeatme.domain.account.repository.AccountRepository;
import com.comeeatme.domain.likes.Likes;
import com.comeeatme.domain.likes.response.LikeCount;
import com.comeeatme.domain.likes.response.LikedResult;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.repository.MemberRepository;
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
class LikesRepositoryTest {

    @Autowired
    private LikesRepository likesRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void findByPostAndMember_Present() {
        // given
        Likes like = likesRepository.saveAndFlush(
                Likes.builder()
                        .post(Post.builder().id(1L).build())
                        .member(Member.builder().id(2L).build())
                        .build());

        // when
        Optional<Likes> foundLike = likesRepository.findByPostAndMember(
                Post.builder().id(1L).build(), Member.builder().id(2L).build());

        // then
        assertThat(foundLike).isPresent();
    }

    @Test
    void findByPostAndMember_Empty() {
        // given
        likesRepository.saveAllAndFlush(List.of(
                Likes.builder() // Not equal post id
                        .post(Post.builder().id(2L).build())
                        .member(Member.builder().id(2L).build())
                        .build(),
                Likes.builder() // Not equal member id
                        .post(Post.builder().id(1L).build())
                        .member(Member.builder().id(3L).build())
                        .build()
        ));

        // when
        Optional<Likes> foundLike = likesRepository.findByPostAndMember(
                Post.builder().id(1L).build(), Member.builder().id(2L).build());

        // then
        assertThat(foundLike).isEmpty();
    }

    @Test
    void countByPost() {
        // given
        likesRepository.saveAllAndFlush(List.of(
                Likes.builder()
                        .post(Post.builder().id(2L).build())
                        .member(Member.builder().id(2L).build())
                        .build(),
                Likes.builder()
                        .post(Post.builder().id(2L).build())
                        .member(Member.builder().id(3L).build())
                        .build(),
                Likes.builder() // 다른 Post ID -> count 에 포함 X
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
    void countsGroupByPosts() {
        // given
        likesRepository.saveAll(List.of(
                Likes.builder()
                        .post(Post.builder().id(1L).build())
                        .member(Member.builder().id(1L).build())
                        .build(),
                Likes.builder()
                        .post(Post.builder().id(1L).build())
                        .member(Member.builder().id(2L).build())
                        .build(),
                Likes.builder() // 다른 Post ID -> count 에 포함 X
                        .post(Post.builder().id(1L).build())
                        .member(Member.builder().id(3L).build())
                        .build(),
                Likes.builder()
                        .post(Post.builder().id(2L).build())
                        .member(Member.builder().id(1L).build())
                        .build(),
                Likes.builder()
                        .post(Post.builder().id(2L).build())
                        .member(Member.builder().id(2L).build())
                        .build(),
                Likes.builder() // 다른 Post ID -> count 에 포함 X
                        .post(Post.builder().id(3L).build())
                        .member(Member.builder().id(1L).build())
                        .build()
        ));

        // when
        List<LikeCount> counts = likesRepository.countsGroupByPosts(List.of(
                Post.builder().id(1L).build(),
                Post.builder().id(2L).build()
        ));

        // then
        counts.sort((o1, o2) -> (int) (o1.getPostId() - o2.getPostId()));
        assertThat(counts).extracting("postId").containsExactly(1L, 2L);
        assertThat(counts).extracting("count").containsExactly(3L, 2L);
    }

    @Test
    void existsByPostIdsAndUsername() {
        // given
        Member member = memberRepository.save(Member.builder()
                .nickname("nickname-1")
                .introduction("introduction")
                .build());
        Account account = accountRepository.save(
                Account.builder()
                        .username("username")
                        .member(member)
                        .build());
        likesRepository.saveAll(List.of(
                Likes.builder()
                        .post(Post.builder().id(1L).build())
                        .member(member)
                        .build(),
                Likes.builder()
                        .post(Post.builder().id(2L).build())
                        .member(member)
                        .build()
        ));

        // when
        List<LikedResult> likedResults = likesRepository.existsByPostIdsAndUsername(List.of(1L, 2L, 3L), "username");

        // then
        likedResults.sort((o1, o2) -> (int) (o1.getPostId() - o2.getPostId()));
        assertThat(likedResults).extracting("postId").containsExactly(1L, 2L, 3L);
        assertThat(likedResults).extracting("liked").containsExactly(true, true, false);
    }

    @Test
    void existsByPostIdsAndUsername_UsernameNotEqual() {
        // given
        Member member = memberRepository.save(Member.builder()
                .nickname("nickname-1")
                .introduction("introduction")
                .build());
        Account account = accountRepository.save(
                Account.builder()
                        .username("username")
                        .member(member)
                        .build());
        likesRepository.saveAll(List.of(
                Likes.builder()
                        .post(Post.builder().id(1L).build())
                        .member(member)
                        .build(),
                Likes.builder()
                        .post(Post.builder().id(2L).build())
                        .member(member)
                        .build()
        ));

        // when
        List<LikedResult> likedResults = likesRepository.existsByPostIdsAndUsername(List.of(1L, 2L, 3L), "user");

        // then
        likedResults.sort((o1, o2) -> (int) (o1.getPostId() - o2.getPostId()));
        assertThat(likedResults).extracting("postId").containsExactly(1L, 2L, 3L);
        assertThat(likedResults).extracting("liked").containsExactly(false, false, false);
    }

    @Test
    void existsByPostIdsAndMemberId() {
        // given
        Member member = memberRepository.save(Member.builder()
                .nickname("nickname-1")
                .introduction("introduction")
                .build());
        likesRepository.saveAll(List.of(
                Likes.builder()
                        .post(Post.builder().id(1L).build())
                        .member(member)
                        .build(),
                Likes.builder()
                        .post(Post.builder().id(2L).build())
                        .member(member)
                        .build()
        ));

        // when
        List<LikedResult> likedResults = likesRepository.existsByPostIdsAndMemberId(List.of(1L, 2L, 3L), member.getId());

        // then
        likedResults.sort((o1, o2) -> (int) (o1.getPostId() - o2.getPostId()));
        assertThat(likedResults).extracting("postId").containsExactly(1L, 2L, 3L);
        assertThat(likedResults).extracting("liked").containsExactly(true, true, false);
    }

    @Test
    void existsByPostIdsAndMemberId_MemberIdNotEqual() {
        // given
        Member member = memberRepository.save(Member.builder()
                .nickname("nickname-1")
                .introduction("introduction")
                .build());
        likesRepository.saveAll(List.of(
                Likes.builder()
                        .post(Post.builder().id(1L).build())
                        .member(member)
                        .build(),
                Likes.builder()
                        .post(Post.builder().id(2L).build())
                        .member(member)
                        .build()
        ));

        // when
        List<LikedResult> likedResults = likesRepository.existsByPostIdsAndMemberId(
                List.of(1L, 2L, 3L), member.getId() + 1);

        // then
        likedResults.sort((o1, o2) -> (int) (o1.getPostId() - o2.getPostId()));
        assertThat(likedResults).extracting("postId").containsExactly(1L, 2L, 3L);
        assertThat(likedResults).extracting("liked").containsExactly(false, false, false);
    }

    @Test
    void existsByPostAndMember() {
        // given
        likesRepository.save(Likes.builder()
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
        likesRepository.save(Likes.builder()
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
        likesRepository.save(Likes.builder()
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