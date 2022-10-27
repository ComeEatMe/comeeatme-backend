package com.comeeatme.domain.post.repository;


import com.comeeatme.domain.post.PostImage;
import com.comeeatme.domain.restaurant.Restaurant;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface PostImageRepositoryCustom {

    Slice<PostImage> findSliceWithImageByRestaurantAndUseYnIsTrue(Restaurant restaurant, Pageable pageable);

}
