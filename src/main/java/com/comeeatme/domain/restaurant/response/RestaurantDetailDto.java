package com.comeeatme.domain.restaurant.response;

import com.comeeatme.domain.restaurant.OpenInfo;
import com.comeeatme.domain.restaurant.Restaurant;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import java.util.Optional;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RestaurantDetailDto {

    private Long id;

    private String name;

    private String category;

    private AddressDto address;

    public static RestaurantDetailDto of(Restaurant restaurant) {
        return RestaurantDetailDto.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .category(Optional.ofNullable(restaurant.getOpenInfo())
                        .map(OpenInfo::getCategory).orElse(null))
                .addressName(restaurant.getAddress().getName())
                .addressRoadName(restaurant.getAddress().getRoadName())
                .addressX(restaurant.getAddress().getPoint().getX())
                .addressY(restaurant.getAddress().getPoint().getY())
                .build();
    }

    @Builder
    public RestaurantDetailDto(
            Long id,
            String name,
            @Nullable String category,
            @Nullable String addressName,
            @Nullable String addressRoadName,
            @Nullable Double addressX,
            @Nullable Double addressY) {
        this.id = id;
        this.name = name;
        this.category = category;
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
