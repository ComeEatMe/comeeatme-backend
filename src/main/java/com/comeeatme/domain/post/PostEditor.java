package com.comeeatme.domain.post;

import com.comeeatme.domain.restaurant.Restaurant;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
public class PostEditor {

    private Restaurant restaurant;

    private Set<Hashtag> hashtags;

    private String content;

    @Builder
    private PostEditor(Restaurant restaurant, Set<Hashtag> hashtags, String content) {
        this.restaurant = restaurant;
        this.hashtags = hashtags;
        this.content = content;
    }
}
