package com.comeeatme.domain.comment.repository;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.account.repository.AccountRepository;
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

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@Transactional
@Import(TestJpaConfig.class)
class CommentRepositoryCustomTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private RestaurantRepository restaurantRepository;

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

}