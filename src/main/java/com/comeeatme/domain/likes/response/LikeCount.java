package com.comeeatme.domain.likes.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LikeCount {

    private Long postId;

    private Long count;

    @QueryProjection
    public LikeCount(Long postId, Long count) {
        this.postId = postId;
        this.count = count;
    }
}
