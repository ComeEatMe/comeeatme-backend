package com.comeeatme.domain.likes.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LikedResult {

    private Long postId;

    private Boolean liked;

    @Builder
    private LikedResult(Long postId, Boolean liked) {
        this.postId = postId;
        this.liked = liked;
    }
}
