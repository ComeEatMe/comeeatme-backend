package com.comeeatme.domain.favorite.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RestaurantFavorited {

    private Long restaurantId;

    private Boolean favorited;

    @Builder
    private RestaurantFavorited(Long restaurantId, Boolean favorited) {
        this.restaurantId = restaurantId;
        this.favorited = favorited;
    }
}
