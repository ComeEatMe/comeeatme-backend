package com.comeeatme.domain.comment.repository;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.comment.Comment;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.post.Post;
import com.comeeatme.domain.post.repository.PostRepository;
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

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PostRepository postRepository;

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
    void existsByIdAndMember_True() {
        // given
        Comment comment = commentRepository.save(Comment.builder()
                .member(memberRepository.getReferenceById(10L))
                .post(Post.builder().id(1L).build())
                .content("test-comment-content")
                .build());

        // expected
        assertThat(commentRepository.existsByIdAndMember(
                comment.getId(), memberRepository.getReferenceById(10L)
        )).isTrue();
    }

    @Test
    void existsByIdAndMember_IdNotEqual_False() {
        // given
        Comment comment = commentRepository.save(Comment.builder()
                .member(memberRepository.getReferenceById(10L))
                .post(Post.builder().id(1L).build())
                .content("test-comment-content")
                .build());

        // expected
        assertThat(commentRepository.existsByIdAndMember(
                comment.getId() + 1, memberRepository.getReferenceById(10L)
        )).isFalse();
    }

    @Test
    void existsByIdAndMember_MemberNotEqual_False() {
        // given
        Comment comment = commentRepository.save(Comment.builder()
                .member(memberRepository.getReferenceById(10L))
                .post(Post.builder().id(1L).build())
                .content("test-comment-content")
                .build());

        // expected
        assertThat(commentRepository.existsByIdAndMember(
                comment.getId() + 1, memberRepository.getReferenceById(10L + 1L)
        )).isFalse();
    }

    @Test
    void findAllByMemberAndUseYnIsTrue() {
        List<Comment> comments = commentRepository.saveAll(List.of(
                Comment.builder()
                        .member(memberRepository.getReferenceById(1L))
                        .post(postRepository.getReferenceById(10L))
                        .content("content-1")
                        .build(),
                Comment.builder()   // post id different
                        .member(memberRepository.getReferenceById(1L))
                        .post(postRepository.getReferenceById(11L))
                        .content("content-2")
                        .build(),
                Comment.builder()   // parent comment id different
                        .member(memberRepository.getReferenceById(1L))
                        .post(postRepository.getReferenceById(10L))
                        .parent(commentRepository.getReferenceById(20L))
                        .content("content-3")
                        .build(),
                Comment.builder()   // deleted
                        .member(memberRepository.getReferenceById(1L))
                        .post(postRepository.getReferenceById(10L))
                        .content("content-4")
                        .build(),
                Comment.builder()   // member id different
                        .member(memberRepository.getReferenceById(2L))
                        .post(postRepository.getReferenceById(10L))
                        .content("content-5")
                        .build()
        ));

        comments.get(3).delete();

        // when
        List<Comment> result = commentRepository.findAllByMemberAndUseYnIsTrue(memberRepository.getReferenceById(1L));

        // then
        assertThat(result)
                .hasSize(3)
                .extracting("id").containsOnly(
                        comments.get(0).getId(), comments.get(1).getId(), comments.get(2).getId());
    }

}