package com.comeeatme.domain.bookmark.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PostBookmarked {

    private Long postId;

    private Boolean bookmarked;

    @Builder
    private PostBookmarked(Long postId, Boolean bookmarked) {
        this.postId = postId;
        this.bookmarked = bookmarked;
    }

}
