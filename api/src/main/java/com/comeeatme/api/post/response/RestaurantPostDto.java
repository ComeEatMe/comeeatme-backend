package com.comeeatme.api.post.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RestaurantPostDto {

    private Long id;

    private List<String> imageUrls;

    private String content;

    private LocalDateTime createdAt;

    private MemberDto member;

    @Builder
    private RestaurantPostDto(
            Long id,
            List<String> imageUrls,
            String content,
            LocalDateTime createdAt,
            Long memberId,
            String memberNickname,
            @Nullable String memberImageUrl) {
        this.id = id;
        this.imageUrls = imageUrls;
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
    public static class MemberDto {

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
