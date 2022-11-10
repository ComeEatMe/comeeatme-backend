package com.comeeatme.domain.favorite.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FavoriteCount {

    private Long restaurantId;

    private Long count;

    @QueryProjection
    public FavoriteCount(Long restaurantId, Long count) {
        this.restaurantId = restaurantId;
        this.count = count;
    }
}
