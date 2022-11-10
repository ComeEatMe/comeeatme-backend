package com.comeeatme.domain.restaurant.repository;

import com.comeeatme.domain.restaurant.Restaurant;
import com.comeeatme.domain.restaurant.request.RestaurantSearch;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface RestaurantRepositoryCustom {

    Slice<Restaurant> findSliceBySearchAndUseYnIsTrue(Pageable pageable, RestaurantSearch search);

}
