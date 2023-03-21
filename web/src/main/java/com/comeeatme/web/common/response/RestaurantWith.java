package com.comeeatme.web.common.response;

import com.comeeatme.domain.post.Hashtag;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestaurantWith<R> {

    @JsonUnwrapped
    private R restaurant;

    private Boolean favorited;

    private List<Hashtag> hashtags;

    private List<String> imageUrls;

    @Builder
    private RestaurantWith(R restaurant, Boolean favorited, List<Hashtag> hashtags, List<String> imageUrls) {
        this.restaurant = restaurant;
        this.favorited = favorited;
        this.hashtags = hashtags;
        this.imageUrls = imageUrls;
    }

    public static <T> RestaurantWithBuilder<T> restaurant(T restaurant) {
        return RestaurantWith.<T>builder().restaurant(restaurant);
    }

}
