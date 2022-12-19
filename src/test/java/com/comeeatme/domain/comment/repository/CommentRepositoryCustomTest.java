package com.comeeatme.domain.comment.repository;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.comment.Comment;
import com.comeeatme.domain.member.Member;
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
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@Transactional
@Import(TestJpaConfig.class)
class CommentRepositoryCustomTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private EntityManager em;

    @Test
    void findSliceByPostWithMemberAndImage() {
        Member member = memberRepository.saveAndFlush(Member.builder()
                .nickname("test-nickname")
                .introduction("test-introduction")
                .build());
        Post post1 = postRepository.saveAndFlush(Post.builder()
                .member(member)
                .restaurant(restaurantRepository.getReferenceById(1L))
                .content("post-content-1")
                .build());
        Post post2 = postRepository.saveAndFlush(Post.builder()
                .member(member)
                .restaurant(restaurantRepository.getReferenceById(1L))
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

    @Test
    void updateUseYnFalseByPostIn() {
        // given
        commentRepository.saveAll(List.of(
                Comment.builder()
                        .content("content-1")
                        .member(memberRepository.getReferenceById(10L))
                        .post(postRepository.getReferenceById(20L))
                        .build(),
                Comment.builder()   // memberId different
                        .content("content-2")
                        .member(memberRepository.getReferenceById(11L))
                        .post(postRepository.getReferenceById(20L))
                        .build(),
                Comment.builder()   // postId different
                        .content("content-3")
                        .member(memberRepository.getReferenceById(10L))
                        .post(postRepository.getReferenceById(21L))
                        .build()
        ));
        em.flush();
        em.clear();

        // when
        commentRepository.updateUseYnFalseByPostIn(List.of(postRepository.getReferenceById(20L)));

        // then
        List<Comment> comments = commentRepository.findAll();

        List<Comment> postId20Comments = comments.stream()
                .filter(comment -> comment.getPost().getId() == 20L)
                .collect(Collectors.toList());
        assertThat(postId20Comments)
                .hasSize(2)
                .extracting("useYn").containsOnly(false);

        List<Comment> postId21Comments = comments.stream()
                .filter(comment -> comment.getPost().getId() == 21L)
                .collect(Collectors.toList());
        assertThat(postId21Comments)
                .hasSize(1)
                .extracting("useYn").containsOnly(true);
    }

}