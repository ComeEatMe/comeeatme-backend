package com.comeeatme.domain.post.repository;

import com.comeeatme.domain.post.Hashtag;
import com.comeeatme.domain.post.response.RestaurantHashtag;
import com.comeeatme.domain.restaurant.Restaurant;

import java.util.List;

public interface PostHashtagRepositoryCustom {

    List<Hashtag> findHashtagsByRestaurant(Restaurant restaurant);

    List<RestaurantHashtag> findHashtagsByRestaurants(List<Restaurant> restaurants);

}
