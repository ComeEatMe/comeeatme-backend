package com.comeeatme.domain.post.repository;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.address.Address;
import com.comeeatme.domain.address.AddressCode;
import com.comeeatme.domain.address.repository.AddressCodeRepository;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.post.Hashtag;
import com.comeeatme.domain.post.Post;
import com.comeeatme.domain.post.request.PostSearch;
import com.comeeatme.domain.restaurant.Restaurant;
import com.comeeatme.domain.restaurant.repository.RestaurantRepository;
import com.comeeatme.security.account.repository.AccountRepository;
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

    @Autowired
    private AddressCodeRepository addressCodeRepository;

    @Test
    void findSliceWithMemberAndRestaurantBy_IdDescOrder() {
        // given
        Member member = memberRepository.save(Member.builder()
                .nickname("test-nickname")
                .introduction("test-introduction")
                .build());

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
        Slice<Post> posts = postRepository.findSliceWithMemberAndRestaurantBy(pageable, postSearch);

        // then
        List<Post> content = posts.getContent();
        assertThat(content).hasSize(2);
        assertThat(content).extracting("id").containsExactly(post2.getId(), post1.getId());
    }

    @Test
    void findSliceWithMemberAndRestaurantBy_Deleted() {
        // given
        Member member = memberRepository.save(Member.builder()
                .nickname("test-nickname")
                .introduction("test-introduction")
                .build());

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
        Post post = postRepository.save(Post.builder()
                .member(member)
                .restaurant(restaurant)
                .content("test-content")
                .build());
        post.delete();

        // when
        PostSearch postSearch = PostSearch.builder().build();
        PageRequest pageable = PageRequest.of(0, 10);
        Slice<Post> posts = postRepository.findSliceWithMemberAndRestaurantBy(pageable, postSearch);

        // then
        List<Post> content = posts.getContent();
        assertThat(content).isEmpty();
    }

    @Test
    void findSliceWithMemberAndRestaurantBy_Hashtag() {
        // given
        Member member = memberRepository.save(Member.builder()
                .nickname("test-nickname")
                .introduction("test-introduction")
                .build());

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
        Slice<Post> posts = postRepository.findSliceWithMemberAndRestaurantBy(pageable, postSearch);

        // then
        List<Post> content = posts.getContent();
        assertThat(content).hasSize(1);
        assertThat(content).extracting("id").containsExactly(post1.getId());
    }

}