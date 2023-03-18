package com.comeeatme.domain.restaurant.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RestaurantSimpleDto {

    private Long id;

    private String name;

    private String addressName;

    @Builder
    private RestaurantSimpleDto(Long id, String name, String addressName) {
        this.id = id;
        this.name = name;
        this.addressName = addressName;
    }
}
