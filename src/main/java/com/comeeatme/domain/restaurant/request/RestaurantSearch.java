package com.comeeatme.domain.restaurant.request;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class RestaurantSearch {

    private String name;

    private Double x;

    private Double y;

    private Double distance;

}
