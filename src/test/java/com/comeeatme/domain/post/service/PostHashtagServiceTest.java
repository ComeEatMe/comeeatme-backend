package com.comeeatme.domain.post.service;

import com.comeeatme.domain.post.Hashtag;
import com.comeeatme.domain.post.repository.PostHashtagRepository;
import com.comeeatme.domain.post.response.RestaurantHashtag;
import com.comeeatme.domain.restaurant.Restaurant;
import com.comeeatme.domain.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class PostHashtagServiceTest {

    @InjectMocks
    private PostHashtagService postHashtagService;

    @Mock
    private PostHashtagRepository postHashtagRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Test
    void getHashtagsOfRestaurant() {
        // given
        Restaurant restaurant = mock(Restaurant.class);
        given(restaurant.getUseYn()).willReturn(true);
        given(restaurantRepository.findById(1L)).willReturn(Optional.of(restaurant));

        given(postHashtagRepository.findHashtagsByRestaurant(restaurant))
                .willReturn(List.of(Hashtag.CLEANLINESS, Hashtag.COST_EFFECTIVENESS));

        // when
        List<Hashtag> result = postHashtagService.getHashtagsOfRestaurant(1L);

        // then
        assertThat(result)
                .hasSize(2)
                .containsOnly(Hashtag.CLEANLINESS, Hashtag.COST_EFFECTIVENESS);
    }

    @Test
    void getHashtagsOfRestaurants() {
        // given
        Restaurant restaurant1 = mock(Restaurant.class);
        given(restaurant1.getUseYn()).willReturn(true);
        Restaurant restaurant2 = mock(Restaurant.class);
        given(restaurant2.getUseYn()).willReturn(true);
        List<Restaurant> restaurants = List.of(restaurant1, restaurant2);

        given(restaurantRepository.findAllById(List.of(1L, 2L))).willReturn(restaurants);

        given(postHashtagRepository.findHashtagsByRestaurants(restaurants)).willReturn(List.of(
                new RestaurantHashtag(1L, Hashtag.COST_EFFECTIVENESS),
                new RestaurantHashtag(1L, Hashtag.EATING_ALON),
                new RestaurantHashtag(1L, Hashtag.MOODY),
                new RestaurantHashtag(2L, Hashtag.COST_EFFECTIVENESS),
                new RestaurantHashtag(2L, Hashtag.DATE)
        ));

        // when
        Map<Long, List<Hashtag>> result = postHashtagService.getHashtagsOfRestaurants(List.of(1L, 2L));

        // then
        assertThat(result).containsOnlyKeys(1L, 2L);
        assertThat(result.get(1L))
                .containsOnly(Hashtag.COST_EFFECTIVENESS, Hashtag.EATING_ALON, Hashtag.MOODY);
        assertThat(result.get(2L))
                .containsOnly(Hashtag.COST_EFFECTIVENESS, Hashtag.DATE);
    }

}