package com.comeeatme.domain.post;

import com.comeeatme.domain.restaurant.Restaurant;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
public class PostEditor {

    private Restaurant restaurant;

    private Set<PostHashtag> postHashtags;

    private String content;

    @Builder
    private PostEditor(Restaurant restaurant, Set<PostHashtag> postHashtags, String content) {
        this.restaurant = restaurant;
        this.postHashtags = postHashtags;
        this.content = content;
    }
}
