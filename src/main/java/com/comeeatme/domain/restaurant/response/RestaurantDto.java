package com.comeeatme.domain.restaurant.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RestaurantDto {

    private Long id;

    private String name;

    private Integer postCount;

    private Integer favoriteCount;

    private AddressDto address;

    @Builder
    public RestaurantDto(
            Long id,
            String name,
            Integer postCount,
            Integer favoriteCount,
            String addressName,
            String addressRoadName) {
        this.id = id;
        this.name = name;
        this.postCount = postCount;
        this.favoriteCount = favoriteCount;
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
