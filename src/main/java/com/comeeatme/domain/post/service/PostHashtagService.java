package com.comeeatme.domain.post.service;

import com.comeeatme.domain.post.Hashtag;
import com.comeeatme.domain.post.repository.PostHashtagRepository;
import com.comeeatme.domain.post.response.RestaurantHashtag;
import com.comeeatme.domain.restaurant.Restaurant;
import com.comeeatme.domain.restaurant.repository.RestaurantRepository;
import com.comeeatme.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostHashtagService {

    private final PostHashtagRepository postHashtagRepository;

    private final RestaurantRepository restaurantRepository;

    public List<Hashtag> getHashtagsOfRestaurant(Long restaurantId) {
        Restaurant restaurant = getRestaurantById(restaurantId);
        return postHashtagRepository.findHashtagsByRestaurant(restaurant);
    }

    public Map<Long, List<Hashtag>> getHashtagsOfRestaurants(List<Long> restaurantIds) {
        List<Restaurant> restaurants = getRestaurantsByIds(restaurantIds);
        return postHashtagRepository.findHashtagsByRestaurants(restaurants)
                .stream()
                .collect(Collectors.groupingBy(
                        RestaurantHashtag::getRestaurantId,
                        Collectors.mapping(RestaurantHashtag::getHashtag, Collectors.toList())
                ));
    }

    private Restaurant getRestaurantById(Long restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .filter(Restaurant::getUseYn)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant id=" + restaurantId));
    }

    private List<Restaurant> getRestaurantsByIds(List<Long> restaurantIds) {
        List<Restaurant> restaurants = restaurantRepository.findAllById(restaurantIds);
        if (restaurantIds.size() != restaurants.size()) {
            throw new EntityNotFoundException("존재하지 않는 음식점 포함. Restaurant.id=" + restaurantIds);
        }
        boolean containDeleted = restaurants.stream()
                .anyMatch(restaurant -> !restaurant.getUseYn());
        if (containDeleted) {
            throw new EntityNotFoundException("삭제된 음식점 포함. Restaurant.id=" + restaurantIds);
        }
        return restaurants;
    }

}
