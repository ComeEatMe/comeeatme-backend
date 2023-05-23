package com.comeeatme.api.post.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberPostDto {

    private Long id;

    private List<String> imageUrls;

    private String content;

    private LocalDateTime createdAt;

    private Integer commentCount;

    private Integer likeCount;

    private RestaurantDto restaurant;

    @Builder
    private MemberPostDto(
            Long id,
            List<String> imageUrls,
            String content,
            LocalDateTime createdAt,
            Integer commentCount,
            Integer likeCount,
            Long restaurantId,
            String restaurantName) {
        this.id = id;
        this.imageUrls = imageUrls;
        this.content = content;
        this.createdAt = createdAt;
        this.commentCount = commentCount;
        this.likeCount = likeCount;
        this.restaurant = RestaurantDto.builder()
                .id(restaurantId)
                .name(restaurantName)
                .build();
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
