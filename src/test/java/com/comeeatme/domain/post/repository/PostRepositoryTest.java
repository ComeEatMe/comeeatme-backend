package com.comeeatme.domain.post.repository;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.address.Address;
import com.comeeatme.domain.image.Image;
import com.comeeatme.domain.image.repository.ImageRepository;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.post.Post;
import com.comeeatme.domain.restaurant.Restaurant;
import com.comeeatme.domain.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceUnitUtil;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
@Import(TestJpaConfig.class)
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private EntityManager em;

    @Test
    void findSliceWithRestaurantByMemberAndUseYnIsTrue() {
        // given
        Restaurant restaurant = restaurantRepository.save(Restaurant.builder()
                .name("모노끼 야탑점")
                .phone("031-702-2929")
                .address(Address.builder()
                        .name("경기 성남시 분당구 야탑동 353-4")
                        .roadName("경기 성남시 분당구 야탑로69번길 24-6")
                        .build())
                .build());

        List<Post> posts = postRepository.saveAll(List.of(
                Post.builder()
                        .member(Member.builder().id(1L).build())
                        .restaurant(restaurant)
                        .content("content-1")
                        .build(),
                Post.builder()
                        .member(Member.builder().id(2L).build())
                        .restaurant(restaurant)
                        .content("content-2")
                        .build()
        ));

        em.flush();
        em.clear();

        // when
        PageRequest pageRequest = PageRequest.of(0, 10);
        Slice<Post> result = postRepository.findSliceWithRestaurantByMemberAndUseYnIsTrue(
                pageRequest, Member.builder().id(1L).build());

        // then
        List<Post> content = result.getContent();
        assertThat(content)
                .hasSize(1)
                .extracting("id").containsExactly(posts.get(0).getId());

        PersistenceUnitUtil unitUtil = em.getEntityManagerFactory().getPersistenceUnitUtil();
        assertThat(unitUtil.isLoaded(content.get(0).getRestaurant())).isTrue();
    }

    @Test
    void findSliceWithRestaurantByMemberAndUseYnIsTrue_Deleted() {
        // given
        Restaurant restaurant = restaurantRepository.save(Restaurant.builder()
                .name("모노끼 야탑점")
                .phone("031-702-2929")
                .address(Address.builder()
                        .name("경기 성남시 분당구 야탑동 353-4")
                        .roadName("경기 성남시 분당구 야탑로69번길 24-6")
                        .build())
                .build());

        Post post = postRepository.save(
                Post.builder()
                        .member(Member.builder().id(1L).build())
                        .restaurant(restaurant)
                        .content("content-1")
                        .build()
        );
        post.delete();

        em.flush();
        em.clear();

        // when
        PageRequest pageRequest = PageRequest.of(0, 10);
        Slice<Post> result = postRepository.findSliceWithRestaurantByMemberAndUseYnIsTrue(
                pageRequest, Member.builder().id(1L).build());

        // then
        List<Post> content = result.getContent();
        assertThat(content).isEmpty();
    }

    @Test
    void findSliceWithMemberByRestaurantAndUseYnIsTrue() {
        // given
        Member member = memberRepository.save(Member.builder()
                .id(10L)
                .nickname("nickname")
                .introduction("intro")
                .build());

        imageRepository.save(Image.builder()
                .originName("origin-name")
                .storedName("stored-name")
                .url("image-url")
                .member(member)
                .build());

        List<Post> posts = postRepository.saveAll(List.of(
                Post.builder()
                        .restaurant(Restaurant.builder().id(1L).build())
                        .content("content-1")
                        .member(member)
                        .build(),
                Post.builder()
                        .restaurant(Restaurant.builder().id(2L).build())
                        .content("content-1")
                        .member(member)
                        .build()
        ));

        em.flush();
        em.clear();

        // when
        PageRequest pageRequest = PageRequest.of(0, 10);
        Slice<Post> result = postRepository.findSliceWithMemberByRestaurantAndUseYnIsTrue(
                pageRequest, Restaurant.builder().id(1L).build());

        // then
        List<Post> content = result.getContent();
        assertThat(content)
                .hasSize(1)
                .extracting("id").containsExactly(posts.get(0).getId());

        PersistenceUnitUtil unitUtil = em.getEntityManagerFactory().getPersistenceUnitUtil();
        assertThat(unitUtil.isLoaded(content.get(0).getMember())).isTrue();
        assertThat(unitUtil.isLoaded(content.get(0).getMember().getImage())).isTrue();
    }

    @Test
    void findSliceWithMemberByRestaurantAndUseYnIsTrue_Deleted() {
        // given
        Member member = memberRepository.save(Member.builder()
                .id(10L)
                .nickname("nickname")
                .introduction("intro")
                .build());

        imageRepository.save(Image.builder()
                .originName("origin-name")
                .storedName("stored-name")
                .url("image-url")
                .member(member)
                .build());

        Post post = postRepository.save(
                Post.builder()
                        .restaurant(Restaurant.builder().id(1L).build())
                        .content("content-1")
                        .member(member)
                        .build()
        );
        post.delete();

        em.flush();
        em.clear();

        // when
        PageRequest pageRequest = PageRequest.of(0, 10);
        Slice<Post> result = postRepository.findSliceWithMemberByRestaurantAndUseYnIsTrue(
                pageRequest, Restaurant.builder().id(1L).build());

        // then
        List<Post> content = result.getContent();
        assertThat(content).isEmpty();
    }

}