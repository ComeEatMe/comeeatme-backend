package com.comeeatme.domain.post;

import com.comeeatme.domain.restaurant.Restaurant;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
public class PostEditor {

    private Restaurant restaurant;

    private Set<HashTag> hashTags;

    private String content;

    @Builder
    private PostEditor(Restaurant restaurant, Set<HashTag> hashTags, String content) {
        this.restaurant = restaurant;
        this.hashTags = hashTags;
        this.content = content;
    }
}
