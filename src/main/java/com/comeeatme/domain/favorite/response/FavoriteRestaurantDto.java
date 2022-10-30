package com.comeeatme.domain.favorite.response;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FavoriteRestaurantDto {

    private Long id;

    private String name;
}
