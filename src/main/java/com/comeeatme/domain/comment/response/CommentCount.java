package com.comeeatme.domain.comment.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentCount {

    private Long postId;

    private Long count;

    @QueryProjection
    public CommentCount(Long postId, Long count) {
        this.postId = postId;
        this.count = count;
    }
}
