package com.comeeatme.api.restaurant.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Getter
@Builder
@AllArgsConstructor
public class RestaurantSearch {

    @NotBlank
    private String keyword;

    @Max(10)
    @Min(1)
    Integer perImageNum;

}
