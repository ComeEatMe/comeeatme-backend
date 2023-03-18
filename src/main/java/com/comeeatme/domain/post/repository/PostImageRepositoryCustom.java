package com.comeeatme.domain.post.repository;


import com.comeeatme.domain.post.PostImage;
import com.comeeatme.domain.post.response.RestaurantPostImage;
import com.comeeatme.domain.restaurant.Restaurant;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface PostImageRepositoryCustom {

    Slice<PostImage> findSliceWithImageByRestaurantAndUseYnIsTrue(Restaurant restaurant, Pageable pageable);

    List<RestaurantPostImage> findImagesByRestaurantsAndPostUseYnIsTrue(List<Restaurant> restaurants, int perImageNum);

}
