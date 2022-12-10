package com.comeeatme.domain.bookmark.repository;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.address.Address;
import com.comeeatme.domain.address.AddressCode;
import com.comeeatme.domain.address.repository.AddressCodeRepository;
import com.comeeatme.domain.bookmark.Bookmark;
import com.comeeatme.domain.favorite.repository.FavoriteRepository;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.post.Post;
import com.comeeatme.domain.post.repository.PostRepository;
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
import javax.persistence.PersistenceUnitUtil;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
@Import(TestJpaConfig.class)
class BookmarkRepositoryTest {

    @Autowired
    private BookmarkRepository bookmarkRepository;
    @Autowired
    private AddressCodeRepository addressCodeRepository;
    @Autowired
    private RestaurantRepository restaurantRepository;
    @Autowired
    private EntityManagerFactory emf;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private FavoriteRepository favoriteRepository;

    @Test
    void findByPostAndMember() {
        // given
        Bookmark bookmark = bookmarkRepository.save(Bookmark.builder()
                .member(Member.builder().id(10L).build())
                .post(Post.builder().id(20L).build())
                .build());

        // when
        Bookmark result = bookmarkRepository.findByPostAndMember(
                Post.builder().id(20L).build(),
                Member.builder().id(10L).build()
        ).orElseThrow();

        // then
        assertThat(bookmark.getId()).isEqualTo(result.getId());
    }

    @Test
    void findByPostAndMember_MemberNotEqual() {
        // given
        Bookmark bookmark = bookmarkRepository.save(Bookmark.builder()
                .member(Member.builder().id(10L).build())
                .post(Post.builder().id(20L).build())
                .build());

        // expected
        assertThat(bookmarkRepository.findByPostAndMember(
                Post.builder().id(20L).build(),
                Member.builder().id(11L).build()
        )).isEmpty();
    }

    @Test
    void findByMemberAndGroupAndPost_PostNotEqual() {
        // given
        Bookmark bookmark = bookmarkRepository.save(Bookmark.builder()
                .member(Member.builder().id(10L).build())
                .post(Post.builder().id(20L).build())
                .build());

        // expected
        assertThat(bookmarkRepository.findByPostAndMember(
                Post.builder().id(40L).build(),
                Member.builder().id(10L).build()
        )).isEmpty();
    }

    @Test
    void countByMember() {
        // given
        bookmarkRepository.saveAll(List.of(
                Bookmark.builder()
                        .member(Member.builder().id(1L).build())
                        .post(Post.builder().id(2L).build())
                        .build(),
                Bookmark.builder()
                        .member(Member.builder().id(1L).build())
                        .post(Post.builder().id(3L).build())
                        .build(),
                Bookmark.builder()
                        .member(Member.builder().id(2L).build())
                        .post(Post.builder().id(4L).build())
                        .build()
        ));

        // when
        int result = bookmarkRepository.countByMember(Member.builder().id(1L).build());

        // then
        assertThat(result).isEqualTo(2);
    }

    @Test
    void existsByPostAndMember() {
        // given
        bookmarkRepository.save(Bookmark.builder()
                .member(Member.builder().id(1L).build())
                .post(Post.builder().id(2L).build())
                .build());

        // expected
        assertThat(bookmarkRepository.existsByPostAndMember(
                Post.builder().id(2L).build(),
                Member.builder().id(1L).build()
        )).isTrue();
    }

    @Test
    void existsByPostAndMember_MemberNotEqual() {
        // given
        bookmarkRepository.save(Bookmark.builder()
                .member(Member.builder().id(1L).build())
                .post(Post.builder().id(2L).build())
                .build());

        // expected
        assertThat(bookmarkRepository.existsByPostAndMember(
                Post.builder().id(2L).build(),
                Member.builder().id(3L).build()
        )).isFalse();
    }

    @Test
    void existsByPostAndMember_PostNotEqual() {
        // given
        bookmarkRepository.save(Bookmark.builder()
                .member(Member.builder().id(1L).build())
                .post(Post.builder().id(2L).build())
                .build());

        // expected
        assertThat(bookmarkRepository.existsByPostAndMember(
                Post.builder().id(3L).build(),
                Member.builder().id(1L).build()
        )).isFalse();
    }

    @Test
    void findSliceWithByMember() {
        // given
        AddressCode addressCode = addressCodeRepository.save(
                AddressCode.builder()
                        .code("1121510700")
                        .name("경기도 성남시 분당구 야탑동")
                        .fullName("야탑동")
                        .depth(3)
                        .terminal(true)
                        .build()
        );
        Restaurant restaurant = restaurantRepository.save(
                Restaurant.builder()
                        .name("모노끼 야탑점")
                        .phone("")
                        .address(Address.builder()
                                .name("경기 성남시 분당구")
                                .roadName("경기 성남시 분당구 야탑로")
                                .addressCode(addressCode)
                                .build())
                        .build()
        );

        Member member = memberRepository.save(
                Member.builder()
                        .nickname("nickname")
                        .introduction("")
                        .build()
        );

        Post post = postRepository.save(
                Post.builder()
                        .content("content")
                        .member(member)
                        .restaurant(restaurant)
                        .build()
        );

        List<Bookmark> bookmarks = bookmarkRepository.saveAll(List.of(
                Bookmark.builder()
                        .post(post)
                        .member(memberRepository.getReferenceById(10L))
                        .build(),
                Bookmark.builder()
                        .post(post)
                        .member(memberRepository.getReferenceById(11L))
                        .build()
        ));

        // when
        PageRequest pageRequest = PageRequest.of(0, 10);
        Slice<Bookmark> result = bookmarkRepository.findSliceWithByMember(
                pageRequest, memberRepository.getReferenceById(10L));

        // then
        List<Bookmark> content = result.getContent();
        assertThat(content)
                .hasSize(1)
                .extracting("id").containsOnly(bookmarks.get(0).getId());
        Bookmark foundBookmark = content.get(0);
        PersistenceUnitUtil persistenceUnitUtil = emf.getPersistenceUnitUtil();
        assertThat(persistenceUnitUtil.isLoaded(foundBookmark.getPost())).isTrue();
        assertThat(persistenceUnitUtil.isLoaded(foundBookmark.getPost().getRestaurant())).isTrue();
        assertThat(persistenceUnitUtil.isLoaded(foundBookmark.getPost().getMember())).isTrue();
    }

}