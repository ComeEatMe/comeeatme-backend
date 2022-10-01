package com.comeeatme.domain.post.request;

import com.comeeatme.domain.post.HashTag;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Set;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PostCreate {

    @NotNull
    private Long restaurantId;

    @NotNull
    private Set<HashTag> hashTags;

    @NotNull
    @Size(min = 1, max = 10)
    private List<Long> imageIds;

    @NotBlank
    @Size(max = 1000)
    private String content;
}
