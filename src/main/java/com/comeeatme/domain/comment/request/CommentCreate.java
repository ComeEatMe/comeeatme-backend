package com.comeeatme.domain.comment.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentCreate {

    private Long parentId;

    @NotBlank
    @Size(max = 1000)
    private String content;
}
