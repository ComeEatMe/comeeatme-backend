package com.comeeatme.domain.comment.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentDto {

    private Long id;

    private Long parentId;

    private String content;

    private LocalDateTime createdAt;

    private MemberDto member;

    @Builder
    private CommentDto(
            Long id,
            @Nullable Long parentId,
            String content,
            LocalDateTime createdAt,
            Long memberId,
            String memberNickname,
            @Nullable String memberImageUrl) {
        this.id = id;
        this.parentId = parentId;
        this.content = content;
        this.createdAt = createdAt;
        this.member = MemberDto.builder()
                .id(memberId)
                .nickname(memberNickname)
                .imageUrl(memberImageUrl)
                .build();
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static class MemberDto {

        private Long id;

        private String nickname;

        private String imageUrl;

        @Builder
        private MemberDto(Long id, String nickname, @Nullable String imageUrl) {
            this.id = id;
            this.nickname = nickname;
            this.imageUrl = imageUrl;
        }
    }
}
