package com.comeeatme.domain.post.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PostDto {

    private Long id;

    private List<String> imageUrls;

    private String content;

    private LocalDateTime createdAt;

    private Integer commentCount;

    private Integer likeCount;

    private MemberDto member;

    private RestaurantDto restaurant;

    @Builder
    private PostDto(
            Long id,
            List<String> imageUrls,
            String content,
            LocalDateTime createdAt,
            Integer commentCount,
            Integer likeCount,
            Long memberId,
            String memberNickname,
            @Nullable String memberImageUrl,
            Long restaurantId,
            String restaurantName) {
        this.id = id;
        this.imageUrls = imageUrls;
        this.content = content;
        this.createdAt = createdAt;
        this.commentCount = commentCount;
        this.likeCount = likeCount;
        this.member = MemberDto.builder()
                .id(memberId)
                .nickname(memberNickname)
                .imageUrl(memberImageUrl)
                .build();
        this.restaurant = RestaurantDto.builder()
                .id(restaurantId)
                .name(restaurantName)
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

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class RestaurantDto {

        private Long id;

        private String name;

        @Builder
        private RestaurantDto(Long id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
