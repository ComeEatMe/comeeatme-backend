package com.comeeatme.web.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostWith<P> {

    @JsonUnwrapped
    private P post;

    private Boolean liked;

    private Boolean bookmarked;

    @Builder
    private PostWith(P post, Boolean liked, Boolean bookmarked) {
        this.post = post;
        this.liked = liked;
        this.bookmarked = bookmarked;
    }

    public static <T> PostWithBuilder<T> post(T post) {
        return PostWith.<T>builder().post(post);
    }

}
