package com.comeeatme.domain.bookmark.service;

import com.comeeatme.domain.bookmark.repository.BookmarkRepository;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.post.Post;
import com.comeeatme.domain.post.repository.PostRepository;
import com.comeeatme.domain.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BookmarkServiceRaceConditionTest {

    @Autowired
    private BookmarkService bookmarkService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @AfterEach
    void afterEach() {
        memberRepository.deleteAll();
        postRepository.deleteAll();
        restaurantRepository.deleteAll();
        bookmarkRepository.deleteAll();
    }

    @Test
    void bookmark() throws InterruptedException {
        // given
        int memberCount = 100;
        List<Member> members = memberRepository.saveAll(IntStream.range(0, memberCount)
                .mapToObj(i -> Member.builder()
                        .nickname("nickname-" + i)
                        .introduction("")
                        .build()
                ).collect(Collectors.toList())
        );

        Post post = postRepository.save(
                Post.builder()
                        .restaurant(restaurantRepository.getReferenceById(1L))
                        .member(members.get(0))
                        .content("content")
                        .build()
        );

        // when
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            Member member = members.get(i);
            executorService.submit(() -> {
                try {
                    bookmarkService.bookmark(post.getId(), member.getId());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then
        Post foundPost = postRepository.findById(post.getId()).orElseThrow();
        assertThat(foundPost.getBookmarkCount()).isEqualTo(100);
    }

    @Test
    void cancelBookmark() throws InterruptedException {
        // given
        int memberCount = 200;
        List<Member> members = memberRepository.saveAll(IntStream.range(0, memberCount)
                .mapToObj(i -> Member.builder()
                        .nickname("nickname-" + i)
                        .introduction("")
                        .build()
                ).collect(Collectors.toList())
        );

        Post post = postRepository.save(
                Post.builder()
                        .restaurant(restaurantRepository.getReferenceById(1L))
                        .member(members.get(0))
                        .content("content")
                        .build()
        );

        members.forEach(member -> bookmarkService.bookmark(post.getId(), member.getId()));

        // when
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            Member member = members.get(i);
            executorService.submit(() -> {
                try {
                    bookmarkService.cancelBookmark(post.getId(), member.getId());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then
        Post foundPost = postRepository.findById(post.getId()).orElseThrow();
        assertThat(foundPost.getBookmarkCount()).isEqualTo(100);
    }

    @Test
    void deleteAllOfMember() throws InterruptedException {
        // given
        int numMember = 100;
        List<Member> members = memberRepository.saveAll(IntStream.range(0, numMember)
                .mapToObj(i -> Member.builder()
                        .nickname("nickname-" + i)
                        .introduction("")
                        .build()
                ).collect(Collectors.toList())
        );

        int numPost = 3;
        List<Post> posts = postRepository.saveAll(
                IntStream.range(0, numPost)
                        .mapToObj(i -> Post.builder()
                                .member(memberRepository.getReferenceById(100L))
                                .restaurant(restaurantRepository.getReferenceById(200L))
                                .content("content-" + i)
                                .build()
                        ).collect(Collectors.toList())
        );

        for (int i = 0; i < numMember; i++) {
            Member member = members.get(i);
            for (int j = 0; j < numPost; j++) {
                Post post = posts.get(j);
                bookmarkService.bookmark(post.getId(), member.getId());
            }
        }

        // when
        int threadCount = numMember;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            Member member = members.get(i);
            executorService.submit(() -> {
                try {
                    bookmarkService.deleteAllOfMember(member.getId());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then
        List<Post> foundPosts = postRepository.findAll();
        for (Post foundPost : foundPosts) {
            assertThat(foundPost.getBookmarkCount()).isZero();
        }
    }


}