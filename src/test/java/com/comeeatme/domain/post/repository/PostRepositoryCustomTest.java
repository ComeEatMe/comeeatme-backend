package com.comeeatme.domain.post.repository;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.account.Account;
import com.comeeatme.domain.account.repository.AccountRepository;
import com.comeeatme.domain.address.Address;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.post.Hashtag;
import com.comeeatme.domain.post.Post;
import com.comeeatme.domain.post.request.PostSearch;
import com.comeeatme.domain.restaurant.Restaurant;
import com.comeeatme.domain.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
@Import(TestJpaConfig.class)
class PostRepositoryCustomTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Test
    void existsByIdAndUsernameAndUseYnIsTrue_True() {
        // given
        Member member = memberRepository.save(Member.builder()
                .nickname("test-nickname")
                .introduction("test-introduction")
                .build());
        Account account1 = accountRepository.save(Account.builder()
                .username("test-username-1")
                .member(member)
                .build());
        Account account2 = accountRepository.save(Account.builder()
                .username("test-username-2")
                .member(member)
                .build());
        Post post = postRepository.save(Post.builder()
                .member(member)
                .restaurant(Restaurant.builder().id(10L).build())
                .content("test-content")
                .build());


        // expected
        assertThat(postRepository.existsByIdAndUsernameAndUseYnIsTrue(
                post.getId(), "test-username-1")).isTrue();
        assertThat(postRepository.existsByIdAndUsernameAndUseYnIsTrue(
                post.getId(), "test-username-2")).isTrue();
    }

    @Test
    void findAllWithMemberAndRestaurant_IdDescOrder() {
        // given
        Member member = memberRepository.save(Member.builder()
                .nickname("test-nickname")
                .introduction("test-introduction")
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
                .build());
        Post post1 = postRepository.save(Post.builder()
                .member(member)
                .restaurant(restaurant)
                .content("test-content")
                .build());
        Post post2 = postRepository.save(Post.builder()
                .member(member)
                .restaurant(restaurant)
                .content("test-content")
                .build());

        // when
        PostSearch postSearch = PostSearch.builder().build();
        PageRequest pageable = PageRequest.of(0, 10);
        Slice<Post> posts = postRepository.findAllWithMemberAndRestaurant(pageable, postSearch);

        // then
        List<Post> content = posts.getContent();
        assertThat(content).hasSize(2);
        assertThat(content).extracting("id").containsExactly(post2.getId(), post1.getId());
    }

    @Test
    void findAllWithMemberAndRestaurant_Deleted() {
        // given
        Member member = memberRepository.save(Member.builder()
                .nickname("test-nickname")
                .introduction("test-introduction")
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
                .build());
        Post post = postRepository.save(Post.builder()
                .member(member)
                .restaurant(restaurant)
                .content("test-content")
                .build());
        post.delete();

        // when
        PostSearch postSearch = PostSearch.builder().build();
        PageRequest pageable = PageRequest.of(0, 10);
        Slice<Post> posts = postRepository.findAllWithMemberAndRestaurant(pageable, postSearch);

        // then
        List<Post> content = posts.getContent();
        assertThat(content).isEmpty();
    }

    @Test
    void findAllWithMemberAndRestaurant_RestaurantId() {
        // given
        Member member = memberRepository.save(Member.builder()
                .nickname("test-nickname")
                .introduction("test-introduction")
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
                .build());
        Post post1 = postRepository.save(Post.builder()
                .member(member)
                .restaurant(restaurant)
                .content("test-content")
                .build());
        Post post2 = postRepository.save(Post.builder()
                .member(member)
                .restaurant(Restaurant.builder().id(restaurant.getId() + 1L).build())
                .content("test-content")
                .build());

        // when
        PostSearch postSearch = PostSearch.builder().restaurantId(restaurant.getId()).build();
        PageRequest pageable = PageRequest.of(0, 10);
        Slice<Post> posts = postRepository.findAllWithMemberAndRestaurant(pageable, postSearch);

        // then
        List<Post> content = posts.getContent();
        assertThat(content).hasSize(1);
        assertThat(content).extracting("id").containsExactly(post1.getId());
    }

    @Test
    void findAllWithMemberAndRestaurant_MemberId() {
        // given
        Member member1 = memberRepository.save(Member.builder()
                .nickname("nickname-1")
                .introduction("test-introduction")
                .build());
        Member member2 = memberRepository.save(Member.builder()
                .nickname("nickname-2")
                .introduction("test-introduction")
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
                .build());
        Post post1 = postRepository.save(Post.builder()
                .member(member1)
                .restaurant(restaurant)
                .content("test-content")
                .build());
        Post post2 = postRepository.save(Post.builder()
                .member(member2)
                .restaurant(restaurant)
                .content("test-content")
                .build());

        // when
        PostSearch postSearch = PostSearch.builder().memberId(member1.getId()).build();
        PageRequest pageable = PageRequest.of(0, 10);
        Slice<Post> posts = postRepository.findAllWithMemberAndRestaurant(pageable, postSearch);

        // then
        List<Post> content = posts.getContent();
        assertThat(content).hasSize(1);
        assertThat(content).extracting("id").containsExactly(post1.getId());
    }

    @Test
    void findAllWithMemberAndRestaurant_Hashtag() {
        // given
        Member member = memberRepository.save(Member.builder()
                .nickname("test-nickname")
                .introduction("test-introduction")
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
                .build());
        Post post1 = postRepository.save(Post.builder()
                .member(member)
                .restaurant(restaurant)
                .content("test-content")
                .build());
        post1.addHashtag(Hashtag.EATING_ALON);
        post1.addHashtag(Hashtag.COST_EFFECTIVENESS);
        Post post2 = postRepository.save(Post.builder()
                .member(member)
                .restaurant(restaurant)
                .content("test-content")
                .build());
        post1.addHashtag(Hashtag.EATING_ALON);

        // when
        PostSearch postSearch = PostSearch.builder()
                .hashtags(Set.of(Hashtag.EATING_ALON, Hashtag.COST_EFFECTIVENESS)).build();
        PageRequest pageable = PageRequest.of(0, 10);
        Slice<Post> posts = postRepository.findAllWithMemberAndRestaurant(pageable, postSearch);

        // then
        List<Post> content = posts.getContent();
        assertThat(content).hasSize(1);
        assertThat(content).extracting("id").containsExactly(post1.getId());
    }

}