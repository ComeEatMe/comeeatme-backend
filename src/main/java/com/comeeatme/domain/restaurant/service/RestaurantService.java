package com.comeeatme.domain.restaurant.service;

import com.comeeatme.domain.favorite.repository.FavoriteRepository;
import com.comeeatme.domain.favorite.response.FavoriteCount;
import com.comeeatme.domain.post.Hashtag;
import com.comeeatme.domain.post.repository.PostRepository;
import com.comeeatme.domain.restaurant.Restaurant;
import com.comeeatme.domain.restaurant.repository.RestaurantRepository;
import com.comeeatme.domain.restaurant.request.RestaurantSearch;
import com.comeeatme.domain.restaurant.response.RestaurantDetailDto;
import com.comeeatme.domain.restaurant.response.RestaurantDto;
import com.comeeatme.domain.restaurant.response.RestaurantSimpleDto;
import com.comeeatme.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    private final FavoriteRepository favoriteRepository;

    private final PostRepository postRepository;

    public Slice<RestaurantSimpleDto> getSimpleList(Pageable pageable, String name) {
        return restaurantRepository.findSliceByNameStartingWithAndUseYnIsTrue(pageable, name)
                .map(restaurant -> RestaurantSimpleDto.builder()
                        .id(restaurant.getId())
                        .name(restaurant.getName())
                        .addressName(restaurant.getAddress().getName())
                        .build()
                );
    }

    public Slice<RestaurantDto> getList(Pageable pageable, RestaurantSearch restaurantSearch) {
        Slice<Restaurant> restaurants = restaurantRepository.findSliceBySearchAndUseYnIsTrue(
                pageable, restaurantSearch);
        Map<Long, Long> restaurantIdToFavoriteCount = getRestaurantIdToFavoriteCount(restaurants.getContent());
        return restaurants
                .map(restaurant -> RestaurantDto.builder()
                        .id(restaurant.getId())
                        .name(restaurant.getName())
                        .favoriteCount(restaurantIdToFavoriteCount.getOrDefault(restaurant.getId(), 0L).intValue())
                        .addressName(restaurant.getAddress().getName())
                        .addressRoadName(restaurant.getAddress().getRoadName())
                        .addressX(restaurant.getAddress().getLocation().getX())
                        .addressY(restaurant.getAddress().getLocation().getY())
                        .build()
                );
    }

    public RestaurantDetailDto get(Long restaurantId) {
        Restaurant restaurant = getRestaurantById(restaurantId);
        int favoriteCount = (int) favoriteRepository.countByRestaurant(restaurant);
        List<Hashtag> hashtags = postRepository.findAllHashtagByRestaurant(restaurant);
        return RestaurantDetailDto.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .favoriteCount(favoriteCount)
                .hashtags(hashtags)
                .addressName(restaurant.getAddress().getName())
                .addressRoadName(restaurant.getAddress().getRoadName())
                .addressX(restaurant.getAddress().getLocation().getX())
                .addressY(restaurant.getAddress().getLocation().getY())
                .build();
    }

    private Restaurant getRestaurantById(Long id) {
        return restaurantRepository.findById(id)
                .filter(Restaurant::getUseYn)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant.id=" + id));
    }

    private Map<Long, Long> getRestaurantIdToFavoriteCount(List<Restaurant> restaurants) {
        return favoriteRepository.countsGroupByRestaurants(restaurants)
                .stream()
                .collect(Collectors.toMap(FavoriteCount::getRestaurantId, FavoriteCount::getCount));
    }

}
