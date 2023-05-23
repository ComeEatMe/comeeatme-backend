package com.comeeatme.domain.post.request;

import com.comeeatme.domain.post.Hashtag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
@AllArgsConstructor
public class PostSearch {

    private Set<Hashtag> hashtags;

}
