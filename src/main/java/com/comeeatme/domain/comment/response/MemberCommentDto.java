package com.comeeatme.domain.comment.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberCommentDto {

    private Long id;

    private String content;

    private LocalDateTime createdAt;

    private PostDto post;

    @Builder
    private MemberCommentDto(
            Long id, String content, LocalDateTime createdAt,
            Long postId, String postContent, String postImageUrl) {
        this.id = id;
        this.content = content;
        this.createdAt = createdAt;
        this.post = PostDto.builder()
                .id(postId)
                .content(postContent)
                .imageUrl(postImageUrl)
                .build();
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class PostDto {

        private Long id;

        private String content;

        private String imageUrl;

        @Builder
        private PostDto(Long id, String content, String imageUrl) {
            this.id = id;
            this.content = content;
            this.imageUrl = imageUrl;
        }
    }

}
