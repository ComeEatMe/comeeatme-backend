package com.comeeatme.domain.restaurant.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RestaurantDetailDto {

    private Long id;

    private String name;

    private AddressDto address;

    private Integer favoriteCount;

    @Builder
    public RestaurantDetailDto(
            Long id,
            String name,
            Integer favoriteCount,
            String addressName,
            String addressRoadName,
            Double addressX,
            Double addressY) {
        this.id = id;
        this.name = name;
        this.favoriteCount = favoriteCount;
        this.address = AddressDto.builder()
                .name(addressName)
                .roadName(addressRoadName)
                .x(addressX)
                .y(addressY)
                .build();
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class AddressDto {

        private String name;

        private String roadName;

        private Double x;

        private Double y;

        @Builder
        private AddressDto(String name, String roadName, Double x, Double y) {
            this.name = name;
            this.roadName = roadName;
            this.x = x;
            this.y = y;
        }
    }
}
