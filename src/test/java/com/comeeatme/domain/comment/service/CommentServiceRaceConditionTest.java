package com.comeeatme.domain.comment.service;

import com.comeeatme.domain.comment.repository.CommentRepository;
import com.comeeatme.domain.comment.request.CommentCreate;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.post.Post;
import com.comeeatme.domain.post.repository.PostRepository;
import com.comeeatme.domain.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CommentServiceRaceConditionTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private CommentRepository commentRepository;

    @AfterEach
    void afterEach() {
        memberRepository.deleteAll();
        postRepository.deleteAll();
        restaurantRepository.deleteAll();
        commentRepository.deleteAll();
    }

    @Test
    void create() throws InterruptedException {
        // given
        Member member = memberRepository.save(
                Member.builder()
                        .nickname("떡볶이")
                        .introduction("")
                        .build()
        );
        Post post = postRepository.save(
                Post.builder()
                        .member(member)
                        .restaurant(restaurantRepository.getReferenceById(10L))
                        .content("content")
                        .build()
        );

        // when
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    CommentCreate commentCreate = CommentCreate.builder()
                            .parentId(null)
                            .content("comment")
                            .build();
                    commentService.create(commentCreate, member.getId(), post.getId());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then
        Post foundPost = postRepository.findById(post.getId()).orElseThrow();
        assertThat(foundPost.getCommentCount()).isEqualTo(100);
    }

    @Test
    void delete() throws InterruptedException {
        // given
        Member member = memberRepository.save(
                Member.builder()
                        .nickname("떡볶이")
                        .introduction("")
                        .build()
        );
        Post post = postRepository.save(
                Post.builder()
                        .member(member)
                        .restaurant(restaurantRepository.getReferenceById(10L))
                        .content("content")
                        .build()
        );

        int commentCount = 200;
        List<Long> commentIds = new ArrayList<>();
        for (int i = 0; i < commentCount; i++) {
            CommentCreate commentCreate = CommentCreate.builder()
                    .parentId(null)
                    .content("comment-" + i)
                    .build();
            Long commentId = commentService.create(commentCreate, member.getId(), post.getId()).getId();
            commentIds.add(commentId);
        }

        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            Long commentId = commentIds.get(i);
            executorService.submit(() -> {
                try {
                    commentService.delete(commentId);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then
        Post foundPost = postRepository.findById(post.getId()).orElseThrow();
        assertThat(foundPost.getCommentCount()).isEqualTo(100);
    }

}