package com.comeeatme.domain.post.response;

import com.comeeatme.domain.comment.response.CommentCount;
import com.comeeatme.domain.image.Image;
import com.comeeatme.domain.like.response.LikeCount;
import com.comeeatme.domain.post.Post;
import com.comeeatme.domain.post.PostImage;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PostDto {

    private Long id;

    private List<String> imageUrls;

    private String content;

    private LocalDateTime createdAt;

    private Long commentCount;

    private Long likeCount;

    private MemberDto member;

    private RestaurantDto restaurant;

    public static PostDto of(Post post, List<PostImage> postImages, CommentCount commentCount, LikeCount likeCount) {
        return PostDto.builder()
                .id(post.getId())
                .imageUrls(postImages.stream()
                        .map(PostImage::getImage)
                        .map(Image::getUrl)
                        .collect(Collectors.toList()))
                .content(post.getContent())
                .createdAt(post.getCreatedAt())
                .commentCount(commentCount.getCount())
                .likeCount(likeCount.getCount())
                .memberId(post.getMember().getId())
                .memberNickname(post.getMember().getNickname())
                .memberImageUrl(Optional.ofNullable(post.getMember().getImage())
                        .filter(Image::getUseYn)
                        .map(Image::getUrl)
                        .orElse(null))
                .restaurantId(post.getRestaurant().getId())
                .restaurantName(post.getRestaurant().getName())
                .build();
    }

    @Builder
    private PostDto(
            Long id,
            List<String> imageUrls,
            String content,
            LocalDateTime createdAt,
            Long commentCount,
            Long likeCount,
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

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static class RestaurantDto {

        private Long id;

        private String name;

        @Builder
        private RestaurantDto(Long id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
