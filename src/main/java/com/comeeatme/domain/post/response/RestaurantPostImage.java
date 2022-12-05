package com.comeeatme.domain.post.response;

import lombok.Getter;

@Getter
public class RestaurantPostImage {

    private Long restaurantId;

    private Long postImageId;

    public RestaurantPostImage(Long restaurantId, Long postImageId) {
        this.restaurantId = restaurantId;
        this.postImageId = postImageId;
    }

}
