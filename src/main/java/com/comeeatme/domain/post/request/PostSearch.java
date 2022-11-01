package com.comeeatme.domain.post.request;

import com.comeeatme.domain.post.Hashtag;
import lombok.*;

import java.util.Set;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PostSearch {

    private Set<Hashtag> hashtags;

}
