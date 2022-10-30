package com.comeeatme.api.common.response;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WithLikedBookmarked<P> {

    @JsonUnwrapped
    private P post;

    private Boolean liked;

    private Boolean bookmarked;

    @Builder
    private WithLikedBookmarked(P post, Boolean liked, Boolean bookmarked) {
        this.post = post;
        this.liked = liked;
        this.bookmarked = bookmarked;
    }

}
