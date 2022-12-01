package com.comeeatme.domain.post;

import com.comeeatme.domain.restaurant.Restaurant;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PostEditor {

    private Restaurant restaurant;

    private String content;

    @Builder
    private PostEditor(Restaurant restaurant, String content) {
        this.restaurant = restaurant;
        this.content = content;
    }
}
