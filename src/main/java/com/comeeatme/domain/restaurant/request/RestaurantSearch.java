package com.comeeatme.domain.restaurant.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotBlank;

@Getter
@Builder
@AllArgsConstructor
public class RestaurantSearch {

    @NotBlank
    private String keyword;

}
