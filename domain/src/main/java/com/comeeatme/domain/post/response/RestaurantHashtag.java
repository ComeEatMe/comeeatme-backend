package com.comeeatme.domain.post.response;

import com.comeeatme.domain.post.Hashtag;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class RestaurantHashtag {

    private Long restaurantId;

    private Hashtag hashtag;

    @QueryProjection
    public RestaurantHashtag(Long restaurantId, Hashtag hashtag) {
        this.restaurantId = restaurantId;
        this.hashtag = hashtag;
    }

}
