package com.comeeatme.api.favorite.response;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class FavoriteGroupDto {

    private String name;

    private Integer favoriteCount;
}
