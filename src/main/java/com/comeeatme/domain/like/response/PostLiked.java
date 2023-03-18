package com.comeeatme.domain.like.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PostLiked {

    private Long postId;

    private Boolean liked;

    @Builder
    private PostLiked(Long postId, Boolean liked) {
        this.postId = postId;
        this.liked = liked;
    }
}
