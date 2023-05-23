package com.comeeatme.api.like.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LikeResult {

    private Long postId;

    private Boolean liked;

    private Long count;

    @Builder
    private LikeResult(Long postId, Boolean liked, Long count) {
        this.postId = postId;
        this.liked = liked;
        this.count = count;
    }
}
