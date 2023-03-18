package com.comeeatme.domain.post.response;

import com.comeeatme.domain.post.Hashtag;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PostDetailDto {

    private Long id;

    private List<String> imageUrls;

    private String content;

    private List<Hashtag> hashtags;

    private LocalDateTime createdAt;

    private Integer commentCount;

    private Integer likeCount;

    private MemberDto member;

    private RestaurantDto restaurant;

    @Builder
    private PostDetailDto(
            Long id,
            List<String> imageUrls,
            String content,
            List<Hashtag> hashtags,
            LocalDateTime createdAt,
            Integer commentCount,
            Integer likeCount,
            Long memberId,
            String memberNickname,
            @Nullable String memberImageUrl,
            Long restaurantId,
            String restaurantName,
            String restaurantAddressName,
            Double restaurantAddressX,
            Double restaurantAddressY) {
        this.id = id;
        this.imageUrls = imageUrls;
        this.content = content;
        this.hashtags = hashtags;
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
                .address(AddressDto.builder()
                        .name(restaurantAddressName)
                        .x(restaurantAddressX)
                        .y(restaurantAddressY)
                        .build())
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

        private AddressDto address;

        @Builder
        private RestaurantDto(Long id, String name, AddressDto address) {
            this.id = id;
            this.name = name;
            this.address = address;
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class AddressDto {

        private String name;

        private Double x;

        private Double y;

        @Builder
        private AddressDto(String name, Double x, Double y) {
            this.name = name;
            this.x = x;
            this.y = y;
        }
    }

}
