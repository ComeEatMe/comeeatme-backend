package com.comeeatme.domain.restaurant.response;

import com.comeeatme.domain.post.Hashtag;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RestaurantDetailDto {

    private Long id;

    private String name;

    private Integer favoriteCount;

    private List<Hashtag> hashtags;

    private AddressDto address;

    @Builder
    public RestaurantDetailDto(
            Long id,
            String name,
            Integer favoriteCount,
            List<Hashtag> hashtags,
            String addressName,
            String addressRoadName) {
        this.id = id;
        this.name = name;
        this.favoriteCount = favoriteCount;
        this.hashtags = hashtags;
        this.address = AddressDto.builder()
                .name(addressName)
                .roadName(addressRoadName)
                .build();
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class AddressDto {

        private String name;

        private String roadName;

        @Builder
        private AddressDto(String name, String roadName) {
            this.name = name;
            this.roadName = roadName;
        }
    }
}
