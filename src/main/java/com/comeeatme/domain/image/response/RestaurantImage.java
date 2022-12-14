package com.comeeatme.domain.image.response;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RestaurantImage {

    private Long restaurantId;

    private Long postId;

    private String imageUrl;
}
