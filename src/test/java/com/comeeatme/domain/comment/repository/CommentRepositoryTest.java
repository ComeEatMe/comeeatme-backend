package com.comeeatme.domain.comment.repository;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.account.Account;
import com.comeeatme.domain.account.repository.AccountRepository;
import com.comeeatme.domain.comment.Comment;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.repository.MemberRepository;
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

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
@Import(TestJpaConfig.class)
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PostRepository postRepository;

    @Test
    void existsByIdAndUsernameAndUseYnIsTrue_True() {
        // given
        Member member = memberRepository.save(Member.builder()
                .nickname("test-nickname")
                .introduction("test-introduction")
                .build());
        Account account1 = accountRepository.save(Account.builder()
                .username("test-username-1")
                .member(member)
                .build());
        Account account2 = accountRepository.save(Account.builder()
                .username("test-username-2")
                .member(member)
                .build());
        Comment comment = commentRepository.save(Comment.builder()
                .member(member)
                .post(Post.builder().id(1L).build())
                .content("test-comment-content")
                .build());

        // expected
        assertThat(commentRepository.existsByIdAndUsernameAndUseYnIsTrue(
                comment.getId(), "test-username-1")).isTrue();
        assertThat(commentRepository.existsByIdAndUsernameAndUseYnIsTrue(
                comment.getId(), "test-username-2")).isTrue();
    }

    @Test
    void existsByIdAndUsernameAndUseYnIsTrue_DiffUsername_False() {
        // given
        Member member = memberRepository.save(Member.builder()
                .nickname("test-nickname")
                .introduction("test-introduction")
                .build());
        Account account = accountRepository.save(Account.builder()
                .username("test-username")
                .member(member)
                .build());
        Comment comment = commentRepository.save(Comment.builder()
                .member(member)
                .post(Post.builder().id(1L).build())
                .content("test-comment-content")
                .build());

        // expected
        assertThat(commentRepository.existsByIdAndUsernameAndUseYnIsTrue(
                comment.getId(), "test-username-1")).isFalse();
    }

    @Test
    void existsByIdAndUsernameAndUseYnIsTrue_Deleted_False() {
        // given
        Member member = memberRepository.save(Member.builder()
                .nickname("test-nickname")
                .introduction("test-introduction")
                .build());
        Account account = accountRepository.save(Account.builder()
                .username("test-username")
                .member(member)
                .build());
        Comment comment = commentRepository.save(Comment.builder()
                .member(member)
                .post(Post.builder().id(1L).build())
                .content("test-comment-content")
                .build());
        comment.delete();

        // expected
        assertThat(commentRepository.existsByIdAndUsernameAndUseYnIsTrue(
                comment.getId(), "test-username")).isFalse();
    }

    @Test
    void existsByIdAndPostAndUseYnIsTrue_True() {
        // given
        Comment comment = commentRepository.save(Comment.builder()
                .member(Member.builder().id(1L).build())
                .post(Post.builder().id(2L).build())
                .content("test-comment-content")
                .build());

        // expected
        assertThat(commentRepository.existsByIdAndPostAndUseYnIsTrue(
                comment.getId(), Post.builder().id(2L).build())).isTrue();
    }

    @Test
    void existsByIdAndPostAndUseYnIsTrue_DiffPostId_False() {
        // given
        Comment comment = commentRepository.save(Comment.builder()
                .member(Member.builder().id(1L).build())
                .post(Post.builder().id(1L).build())
                .content("test-comment-content")
                .build());

        // expected
        assertThat(commentRepository.existsByIdAndPostAndUseYnIsTrue(
                comment.getId(), Post.builder().id(2L).build())).isFalse();
    }

    @Test
    void existsByIdAndPostAndUseYnIsTrue_Deleted_False() {
        // given
        Comment comment = commentRepository.save(Comment.builder()
                .member(Member.builder().id(1L).build())
                .post(Post.builder().id(2L).build())
                .content("test-comment-content")
                .build());
        comment.delete();

        // expected
        assertThat(commentRepository.existsByIdAndPostAndUseYnIsTrue(
                comment.getId(), Post.builder().id(2L).build())).isFalse();
    }

    @Test
    void findSliceByPostWithMemberAndImage() {
        Member member = memberRepository.saveAndFlush(Member.builder()
                .nickname("test-nickname")
                .introduction("test-introduction")
                .build());
        Post post1 = postRepository.saveAndFlush(Post.builder()
                .member(member)
                .restaurant(Restaurant.builder().id(1L).build())
                .content("post-content-1")
                .build());
        Post post2 = postRepository.saveAndFlush(Post.builder()
                .member(member)
                .restaurant(Restaurant.builder().id(1L).build())
                .content("post-content-2")
                .build());

        Comment comment1 = commentRepository.saveAndFlush(Comment.builder()
                .content("comment-1")
                .post(post1)
                .member(member)
                .build());
        Comment comment2 = commentRepository.saveAndFlush(Comment.builder()
                .content("comment-2")
                .post(post1)
                .member(member)
                .build());
        Comment comment3 = commentRepository.saveAndFlush(Comment.builder()
                .content("comment-3")
                .post(post1)
                .member(member)
                .parent(comment1)
                .build());
        Comment comment4 = commentRepository.saveAndFlush(Comment.builder()
                .content("comment-4")
                .post(post2)
                .member(member)
                .build());
        PageRequest pageRequest = PageRequest.of(0, 10);

        // when
        Slice<Comment> commentSlice = commentRepository.findSliceByPostWithMemberAndImage(
                pageRequest, post1);

        // then
        assertThat(commentSlice.hasNext()).isFalse();
        assertThat(commentSlice.getPageable()).isEqualTo(pageRequest);
        assertThat(commentSlice.getContent())
                .hasSize(3)
                .extracting("id").containsExactly(comment1.getId(), comment3.getId(), comment2.getId())
        ;
    }
}