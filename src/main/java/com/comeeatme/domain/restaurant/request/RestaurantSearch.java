package com.comeeatme.domain.restaurant.request;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RestaurantSearch {

    private String name;

    private Double x;

    private Double y;

    private Double distance;

}
