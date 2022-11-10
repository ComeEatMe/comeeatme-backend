package com.comeeatme.domain.post.request;

import com.comeeatme.domain.post.Hashtag;
import lombok.*;

import java.util.Set;

@Getter
@Builder
@AllArgsConstructor
public class PostSearch {

    private Set<Hashtag> hashtags;

}
