package com.comeeatme.domain.comment;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CommentEditor {

    private String content;

    @Builder
    private CommentEditor(String content) {
        this.content = content;
    }
}
