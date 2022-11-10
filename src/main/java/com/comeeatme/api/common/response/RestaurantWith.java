package com.comeeatme.api.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestaurantWith<R> {

    @JsonUnwrapped
    private R restaurant;

    private Boolean favorited;

    @Builder
    private RestaurantWith(R restaurant, Boolean favorited) {
        this.restaurant = restaurant;
        this.favorited = favorited;
    }

    public static <T> RestaurantWithBuilder<T> restaurant(T restaurant) {
        return RestaurantWith.<T>builder().restaurant(restaurant);
    }

}
