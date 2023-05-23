package com.comeeatme.domain.post.repository;

import com.comeeatme.domain.common.TestJpaConfig;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.post.Hashtag;
import com.comeeatme.domain.post.Post;
import com.comeeatme.domain.post.response.RestaurantHashtag;
import com.comeeatme.domain.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
@Import(TestJpaConfig.class)
class PostHashtagRepositoryCustomTest {

    @Autowired
    private PostHashtagRepository postHashtagRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void findHashtagsByRestaurant() {
        // given
        List<Post> posts = postRepository.saveAll(List.of(
                Post.builder()
                        .member(memberRepository.getReferenceById(10L))
                        .restaurant(restaurantRepository.getReferenceById(20L))
                        .content("content-1")
                        .build(),
                Post.builder()
                        .member(memberRepository.getReferenceById(10L))
                        .restaurant(restaurantRepository.getReferenceById(20L))
                        .content("content-2")
                        .build()
        ));
        posts.get(0).addHashtag(Hashtag.CLEANLINESS);
        posts.get(0).addHashtag(Hashtag.AROUND_CLOCK);
        posts.get(1).addHashtag(Hashtag.AROUND_CLOCK);
        posts.get(1).addHashtag(Hashtag.EATING_ALON);

        // when
        List<Hashtag> result = postHashtagRepository.findHashtagsByRestaurant(
                restaurantRepository.getReferenceById(20L));

        // then
        assertThat(result)
                .hasSize(3)
                .containsOnly(Hashtag.CLEANLINESS, Hashtag.AROUND_CLOCK, Hashtag.EATING_ALON);
    }

    @Test
    void findHashtagsByRestaurants() {
        // given
        List<Post> posts = postRepository.saveAll(List.of(
                Post.builder()
                        .member(memberRepository.getReferenceById(10L))
                        .restaurant(restaurantRepository.getReferenceById(20L))
                        .content("content-1")
                        .build(),
                Post.builder()
                        .member(memberRepository.getReferenceById(10L))
                        .restaurant(restaurantRepository.getReferenceById(20L))
                        .content("content-2")
                        .build(),
                Post.builder()
                        .member(memberRepository.getReferenceById(10L))
                        .restaurant(restaurantRepository.getReferenceById(21L))
                        .content("content-3")
                        .build()
        ));
        posts.get(0).addHashtag(Hashtag.CLEANLINESS);
        posts.get(0).addHashtag(Hashtag.AROUND_CLOCK);
        posts.get(1).addHashtag(Hashtag.AROUND_CLOCK);
        posts.get(1).addHashtag(Hashtag.EATING_ALON);
        posts.get(2).addHashtag(Hashtag.EATING_ALON);
        posts.get(2).addHashtag(Hashtag.COST_EFFECTIVENESS);

        // when
        List<RestaurantHashtag> result = postHashtagRepository.findHashtagsByRestaurants(
                List.of(restaurantRepository.getReferenceById(20L), restaurantRepository.getReferenceById(21L)));

        // then
        assertThat(result).hasSize(5);
        Map<Long, List<Hashtag>> restaurantIdToHashtags = result.stream()
                .collect(Collectors.groupingBy(RestaurantHashtag::getRestaurantId,
                        Collectors.mapping(RestaurantHashtag::getHashtag, Collectors.toList())));
        assertThat(restaurantIdToHashtags).containsOnlyKeys(20L, 21L);
        assertThat(restaurantIdToHashtags.get(20L)).containsOnly(
                Hashtag.CLEANLINESS, Hashtag.AROUND_CLOCK, Hashtag.EATING_ALON);
        assertThat(restaurantIdToHashtags.get(21L)).containsOnly(
                Hashtag.EATING_ALON, Hashtag.COST_EFFECTIVENESS);
    }
}