package com.comeeatme.api.common.response;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WithFavorited<R> {

    @JsonUnwrapped
    private R restaurant;

    private Boolean favorited;

    @Builder
    private WithFavorited(R restaurant, Boolean favorited) {
        this.restaurant = restaurant;
        this.favorited = favorited;
    }

    public static <T> WithFavoritedBuilder<T> restaurant(T restaurant) {
        return WithFavorited.<T>builder().restaurant(restaurant);
    }

}
