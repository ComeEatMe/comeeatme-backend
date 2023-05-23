package com.comeeatme.api.comment.request;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentCreate {

    private Long parentId;

    @NotBlank
    @Size(max = 1000)
    private String content;
}
