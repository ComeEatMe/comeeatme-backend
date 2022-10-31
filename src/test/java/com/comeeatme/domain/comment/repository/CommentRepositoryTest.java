package com.comeeatme.domain.comment.repository;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.comment.Comment;
import com.comeeatme.domain.member.Member;
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
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

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
    void findAllByPostAndUseYnIsTrue() {
        // given
        List<Comment> comments = commentRepository.saveAllAndFlush(List.of(
                Comment.builder()
                        .member(Member.builder().id(1L).build())
                        .post(Post.builder().id(10L).build())
                        .content("content1")
                        .build(),
                Comment.builder()
                        .member(Member.builder().id(1L).build())
                        .post(Post.builder().id(20L).build())
                        .content("content2")
                        .build(),
                Comment.builder()
                        .member(Member.builder().id(1L).build())
                        .post(Post.builder().id(10L).build())
                        .content("content3")
                        .build()
        ));
        comments.get(2).delete();

        // when
        List<Comment> result = commentRepository.findAllByPostAndUseYnIsTrue(
                Post.builder().id(10L).build());

        // then
        assertThat(result)
                .hasSize(1)
                .extracting("id").containsExactly(comments.get(0).getId());
    }

    @Test
    void countByPost() {
        // given
        List<Comment> comments = commentRepository.saveAll(List.of(
                Comment.builder()
                        .post(Post.builder().id(1L).build())
                        .member(Member.builder().id(1L).build())
                        .content("content")
                        .build(),
                Comment.builder()
                        .post(Post.builder().id(1L).build())
                        .member(Member.builder().id(2L).build())
                        .content("content")
                        .build(),
                Comment.builder()
                        .post(Post.builder().id(2L).build())
                        .member(Member.builder().id(1L).build())
                        .content("content")
                        .build()
        ));
        comments.get(0).delete();

        // when
        long result = commentRepository.countByPostAndUseYnIsTrue(Post.builder().id(1L).build());

        // then
        assertThat(result).isEqualTo(1L);
    }

}