package com.comeeatme.domain.post.request;

import com.comeeatme.domain.post.Hashtag;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Set;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PostCreate {

    @NotNull
    private Long restaurantId;

    @NotNull
    private Set<Hashtag> hashtags;

    @NotNull
    @Size(min = 1, max = 10)
    private List<Long> imageIds;

    @NotBlank
    @Size(max = 2000)
    private String content;
}
