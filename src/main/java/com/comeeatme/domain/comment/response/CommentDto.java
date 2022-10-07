package com.comeeatme.domain.comment.response;

import com.comeeatme.domain.comment.Comment;
import com.comeeatme.domain.images.Images;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentDto {

    private Long id;

    private Long parentId;

    private Boolean deleted;

    private String content;

    private LocalDateTime createdAt;

    private MemberDto member;

    public static CommentDto of(Comment comment) {
        CommentDtoBuilder builder = CommentDto.builder()
                .id(comment.getId())
                .parentId(Optional.ofNullable(comment.getParent())
                        .map(Comment::getId).orElse(null))
                .deleted(!comment.getUseYn());

        if (Boolean.TRUE.equals(comment.getUseYn())) {
            builder
                    .content(comment.getContent())
                    .createdAt(comment.getCreatedAt())
                    .memberId(comment.getMember().getId())
                    .memberNickname(comment.getMember().getNickname())
                    .memberImageUrl(Optional.ofNullable(comment.getMember().getImage())
                            .map(Images::getUrl).orElse(null));
        }

        return builder.build();
    }

    @Builder
    private CommentDto(
            Long id,
            @Nullable Long parentId,
            Boolean deleted,
            String content,
            LocalDateTime createdAt,
            Long memberId,
            String memberNickname,
            @Nullable String memberImageUrl) {
        this.id = id;
        this.parentId = parentId;
        this.deleted = deleted;
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
