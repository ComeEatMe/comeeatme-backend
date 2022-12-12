package com.comeeatme.domain.post.repository;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.address.Address;
import com.comeeatme.domain.address.AddressCode;
import com.comeeatme.domain.address.repository.AddressCodeRepository;
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
import java.util.Optional;

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
    private AddressCodeRepository addressCodeRepository;

    @Autowired
    private EntityManager em;

    @Test
    void findSliceWithRestaurantByMemberAndUseYnIsTrue() {
        // given
        AddressCode addressCode = addressCodeRepository.saveAll(List.of(
                AddressCode.builder()
                        .code("4100000000")
                        .name("경기도")
                        .fullName("경기도")
                        .depth(1)
                        .terminal(false)
                        .build(),
                AddressCode.builder()
                        .code("4113500000")
                        .name("경기도 성남시 분당구")
                        .fullName("성남시 분당구")
                        .depth(2)
                        .terminal(false)
                        .build(),
                AddressCode.builder()
                        .code("1121510700")
                        .name("경기도 성남시 분당구 야탑동")
                        .fullName("야탑동")
                        .depth(3)
                        .terminal(true)
                        .build()
        )).get(2);
        Restaurant restaurant = restaurantRepository.save(Restaurant.builder()
                .name("모노끼 야탑점")
                .phone("031-702-2929")
                .address(Address.builder()
                        .name("경기 성남시 분당구 야탑동 353-4")
                        .roadName("경기 성남시 분당구 야탑로69번길 24-6")
                        .addressCode(addressCode)
                        .build())
                .build());

        List<Post> posts = postRepository.saveAll(List.of(
                Post.builder()
                        .member(memberRepository.getReferenceById(1L))
                        .restaurant(restaurant)
                        .content("content-1")
                        .build(),
                Post.builder()
                        .member(memberRepository.getReferenceById(2L))
                        .restaurant(restaurant)
                        .content("content-2")
                        .build()
        ));

        em.flush();
        em.clear();

        // when
        PageRequest pageRequest = PageRequest.of(0, 10);
        Slice<Post> result = postRepository.findSliceWithRestaurantByMemberAndUseYnIsTrue(
                pageRequest, memberRepository.getReferenceById(1L));

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
        AddressCode addressCode = addressCodeRepository.saveAll(List.of(
                AddressCode.builder()
                        .code("4100000000")
                        .name("경기도")
                        .fullName("경기도")
                        .depth(1)
                        .terminal(false)
                        .build(),
                AddressCode.builder()
                        .code("4113500000")
                        .name("경기도 성남시 분당구")
                        .fullName("성남시 분당구")
                        .depth(2)
                        .terminal(false)
                        .build(),
                AddressCode.builder()
                        .code("1121510700")
                        .name("경기도 성남시 분당구 야탑동")
                        .fullName("야탑동")
                        .depth(3)
                        .terminal(true)
                        .build()
        )).get(2);
        Restaurant restaurant = restaurantRepository.save(Restaurant.builder()
                .name("모노끼 야탑점")
                .phone("031-702-2929")
                .address(Address.builder()
                        .name("경기 성남시 분당구 야탑동 353-4")
                        .roadName("경기 성남시 분당구 야탑로69번길 24-6")
                        .addressCode(addressCode)
                        .build())
                .build());

        Post post = postRepository.save(
                Post.builder()
                        .member(memberRepository.getReferenceById(1L))
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
                pageRequest, memberRepository.getReferenceById(1L));

        // then
        List<Post> content = result.getContent();
        assertThat(content).isEmpty();
    }

    @Test
    void findSliceWithMemberByRestaurantAndUseYnIsTrue() {
        // given
        Member member = memberRepository.save(Member.builder()
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

    @Test
    void findWithPessimisticLockById() {
        // given
        Post post = postRepository.save(
                Post.builder()
                        .member(memberRepository.getReferenceById(1L))
                        .restaurant(Restaurant.builder().id(2L).build())
                        .content("content")
                        .build()
        );

        // when
        Optional<Post> result = postRepository.findWithPessimisticLockById(post.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(post.getId());
    }

    @Test
    void existsByIdAndMember_True() {
        // given
        Member member = memberRepository.save(Member.builder()
                .nickname("test-nickname")
                .introduction("test-introduction")
                .build());
        Post post = postRepository.save(Post.builder()
                .member(member)
                .restaurant(Restaurant.builder().id(10L).build())
                .content("test-content")
                .build());

        // expected
        assertThat(postRepository.existsByIdAndMember(
                post.getId(), member)).isTrue();
    }

    @Test
    void existsByIdAndMember_PostIdNotEqual_False() {
        // given
        Member member = memberRepository.save(Member.builder()
                .nickname("test-nickname")
                .introduction("test-introduction")
                .build());
        Post post = postRepository.save(Post.builder()
                .member(member)
                .restaurant(Restaurant.builder().id(10L).build())
                .content("test-content")
                .build());

        // expected
        assertThat(postRepository.existsByIdAndMember(
                post.getId() + 1L, member)).isFalse();
    }

    @Test
    void existsByIdAndMember_MemberNotEqual_False() {
        // given
        Member member = memberRepository.save(Member.builder()
                .nickname("test-nickname")
                .introduction("test-introduction")
                .build());
        Post post = postRepository.save(Post.builder()
                .member(member)
                .restaurant(Restaurant.builder().id(10L).build())
                .content("test-content")
                .build());

        // expected
        assertThat(postRepository.existsByIdAndMember(
                post.getId() + 1L, memberRepository.getReferenceById(member.getId() + 1L)))
                .isFalse();
    }

    @Test
    void findAllByMemberAndUseYnIsTrue() {
        // given
        List<Post> posts = postRepository.saveAll(List.of(
                Post.builder()
                        .content("content-1")
                        .member(memberRepository.getReferenceById(1L))
                        .restaurant(restaurantRepository.getReferenceById(2L))
                        .build(),
                Post.builder()
                        .content("content-2")
                        .member(memberRepository.getReferenceById(1L))
                        .restaurant(restaurantRepository.getReferenceById(2L))
                        .build(),
                Post.builder()
                        .content("content-3")
                        .member(memberRepository.getReferenceById(2L))
                        .restaurant(restaurantRepository.getReferenceById(2L))
                        .build()
        ));

        posts.get(1).delete();

        // when
        List<Post> result = postRepository.findAllByMemberAndUseYnIsTrue(memberRepository.getReferenceById(1L));

        // then
        assertThat(result)
                .hasSize(1)
                .extracting("id").containsOnly(posts.get(0).getId());
    }

    @Test
    void findAllWithPessimisticLockByIdIn() {
        // given
        List<Post> posts = postRepository.saveAll(List.of(
                Post.builder()
                        .content("content-1")
                        .member(memberRepository.getReferenceById(1L))
                        .restaurant(restaurantRepository.getReferenceById(2L))
                        .build(),
                Post.builder()
                        .content("content-2")
                        .member(memberRepository.getReferenceById(1L))
                        .restaurant(restaurantRepository.getReferenceById(2L))
                        .build(),
                Post.builder()
                        .content("content-3")
                        .member(memberRepository.getReferenceById(1L))
                        .restaurant(restaurantRepository.getReferenceById(2L))
                        .build()
        ));

        // when
        List<Post> result = postRepository.findAllWithPessimisticLockByIdIn(
                List.of(posts.get(0).getId(), posts.get(1).getId()));

        // then
        assertThat(result)
                .hasSize(2)
                .extracting("id").containsOnly(posts.get(0).getId(), posts.get(1).getId());
    }

}