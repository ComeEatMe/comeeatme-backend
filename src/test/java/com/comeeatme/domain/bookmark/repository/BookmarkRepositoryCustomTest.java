package com.comeeatme.domain.bookmark.repository;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.address.Address;
import com.comeeatme.domain.bookmark.Bookmark;
import com.comeeatme.domain.bookmark.BookmarkGroup;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.post.Post;
import com.comeeatme.domain.post.repository.PostRepository;
import com.comeeatme.domain.restaurant.OpenInfo;
import com.comeeatme.domain.restaurant.Restaurant;
import com.comeeatme.domain.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManagerFactory;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
@Import(TestJpaConfig.class)
class BookmarkRepositoryCustomTest {

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EntityManagerFactory emf;

    @Test
    void findByMemberIdAndPostIds() {
        // given
        List<Bookmark> bookmarks = bookmarkRepository.saveAll(List.of(
                Bookmark.builder()
                        .member(Member.builder().id(10L).build())
                        .post(Post.builder().id(1L).build())
                        .build(),
                Bookmark.builder()
                        .member(Member.builder().id(10L).build())
                        .post(Post.builder().id(2L).build())
                        .build(),
                Bookmark.builder()
                        .member(Member.builder().id(11L).build())
                        .post(Post.builder().id(3L).build())
                        .build()
        ));

        // when
        List<Bookmark> result = bookmarkRepository.findByMemberIdAndPostIds(10L, List.of(1L, 2L, 3L));

        // then
        result.sort((o1, o2) -> (int) (o1.getPost().getId() - o2.getPost().getId()));
        assertThat(result)
                .hasSize(2)
                .extracting("id").containsExactly(bookmarks.get(0).getId(), bookmarks.get(1).getId());
    }

    @Test
    void deleteAllByPost() {
        // given
        List<Bookmark> bookmarks = bookmarkRepository.saveAll(List.of(
                Bookmark.builder()
                        .member(Member.builder().id(10L).build())
                        .post(Post.builder().id(1L).build())
                        .build(),
                Bookmark.builder()
                        .member(Member.builder().id(10L).build())
                        .post(Post.builder().id(2L).build())
                        .build(),
                Bookmark.builder()
                        .member(Member.builder().id(11L).build())
                        .post(Post.builder().id(1L).build())
                        .build()
        ));

        // when
        bookmarkRepository.deleteAllByPost(Post.builder().id(1L).build());

        // then
        List<Bookmark> foundBookmarks = bookmarkRepository.findAll();
        assertThat(foundBookmarks)
                .hasSize(1)
                .extracting("id").containsOnly(bookmarks.get(1).getId());
    }

    @Test
    void findSliceWithByMemberAndGroup_FetchJoin_Slice() {
        // given
        Member member = memberRepository.save(Member.builder()
                .nickname("nickname")
                .introduction("")
                .build());

        Restaurant restaurant = restaurantRepository.save(Restaurant.builder()
                .name("모노끼 야탑점")
                .phone("031-702-2929")
                .address(Address.builder()
                        .name("경기 성남시 분당구 야탑동 353-4")
                        .roadName("경기 성남시 분당구 야탑로69번길 24-6")
                        .x(211199.96154825)
                        .y(434395.793544651)
                        .build())
                .openInfo(OpenInfo.builder()
                        .id(2L)
                        .build())
                .build());

        List<Post> posts = postRepository.saveAll(List.of(
                Post.builder()
                        .member(member)
                        .restaurant(restaurant)
                        .content("content-1")
                        .build(),
                Post.builder()
                        .member(member)
                        .restaurant(restaurant)
                        .content("content-2")
                        .build()
        ));

        List<Bookmark> bookmarks = posts.stream()
                .map(post -> bookmarkRepository.save(Bookmark.builder()
                        .member(member)
                        .post(post)
                        .group(BookmarkGroup.builder().id(post.getId()).build())
                        .build())
                ).collect(Collectors.toList());

        // when
        PageRequest pageRequest = PageRequest.of(0, 10);
        Slice<Bookmark> result = bookmarkRepository.findSliceWithByMemberAndGroup(
                pageRequest,
                Member.builder().id(member.getId()).build(),
                BookmarkGroup.builder().id(posts.get(0).getId()).build());

        // then
        assertThat(result.hasNext()).isFalse();
        assertThat(result.getPageable()).isEqualTo(pageRequest);
        List<Bookmark> content = result.getContent();
        assertThat(content)
                .hasSize(1)
                .extracting("id").containsOnly(bookmarks.get(0).getId());
        for (Bookmark bookmark : content) {
            assertThat(emf.getPersistenceUnitUtil().isLoaded(bookmark.getMember())).isTrue();
            assertThat(emf.getPersistenceUnitUtil().isLoaded(bookmark.getPost())).isTrue();
            assertThat(emf.getPersistenceUnitUtil().isLoaded(bookmark.getPost().getRestaurant())).isTrue();
        }

    }

    @Test
    void findSliceWithByMemberAndGroup_GroupNull() {
        // given
        Member member = memberRepository.save(Member.builder()
                .nickname("nickname")
                .introduction("")
                .build());

        Restaurant restaurant = restaurantRepository.save(Restaurant.builder()
                .name("모노끼 야탑점")
                .phone("031-702-2929")
                .address(Address.builder()
                        .name("경기 성남시 분당구 야탑동 353-4")
                        .roadName("경기 성남시 분당구 야탑로69번길 24-6")
                        .x(211199.96154825)
                        .y(434395.793544651)
                        .build())
                .openInfo(OpenInfo.builder()
                        .id(2L)
                        .build())
                .build());

        List<Post> posts = postRepository.saveAll(List.of(
                Post.builder()
                        .member(member)
                        .restaurant(restaurant)
                        .content("content-1")
                        .build(),
                Post.builder()
                        .member(member)
                        .restaurant(restaurant)
                        .content("content-2")
                        .build()
        ));

        List<Bookmark> bookmarks = posts.stream()
                .map(post -> bookmarkRepository.save(Bookmark.builder()
                        .member(member)
                        .post(post)
                        .group(BookmarkGroup.builder().id(post.getId()).build())
                        .build())
                ).collect(Collectors.toList());

        // when
        PageRequest pageRequest = PageRequest.of(0, 10);
        Slice<Bookmark> result = bookmarkRepository.findSliceWithByMemberAndGroup(
                pageRequest, Member.builder().id(1L).build(), null);

        // then
        List<Bookmark> content = result.getContent();
        assertThat(content)
                .hasSize(2)
                .extracting("id").containsOnly(bookmarks.get(0).getId(), bookmarks.get(1).getId());
    }

}