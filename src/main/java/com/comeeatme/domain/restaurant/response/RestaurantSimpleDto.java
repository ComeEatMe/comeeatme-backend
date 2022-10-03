package com.comeeatme.domain.restaurant.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class RestaurantSimpleDto {

    private final Long id;

    private final String name;

    private final String addressName;

    @Builder
    private RestaurantSimpleDto(Long id, String name, String addressName) {
        this.id = id;
        this.name = name;
        this.addressName = addressName;
    }
}
