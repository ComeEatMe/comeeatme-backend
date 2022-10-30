package com.comeeatme.api.v1;

import com.comeeatme.api.common.response.ApiResult;
import com.comeeatme.api.common.response.WithFavorited;
import com.comeeatme.domain.account.service.AccountService;
import com.comeeatme.domain.favorite.service.FavoriteService;
import com.comeeatme.domain.restaurant.response.RestaurantDetailDto;
import com.comeeatme.domain.restaurant.response.RestaurantSimpleDto;
import com.comeeatme.domain.restaurant.service.RestaurantService;
import com.comeeatme.security.annotation.CurrentUsername;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/v1")
@RestController
@RequiredArgsConstructor
public class RestaurantController {

    private final AccountService accountService;

    private final RestaurantService restaurantService;

    private final FavoriteService favoriteService;

    @GetMapping("/restaurants/simple")
    public ResponseEntity<ApiResult<Slice<RestaurantSimpleDto>>> getSimpleList(
            Pageable pageable, @RequestParam String name) {
        Slice<RestaurantSimpleDto> simpleList = restaurantService.getSimpleList(pageable, name);
        ApiResult<Slice<RestaurantSimpleDto>> result = ApiResult.success(simpleList);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/restaurants/{restaurantId}")
    public ResponseEntity<ApiResult<WithFavorited<RestaurantDetailDto>>> get(
            @PathVariable Long restaurantId, @CurrentUsername String username) {
        Long memberId = accountService.getMemberId(username);
        RestaurantDetailDto restaurant = restaurantService.get(restaurantId);
        boolean favorited = favoriteService.isFavorite(memberId, restaurant.getId());
        WithFavorited<RestaurantDetailDto> restaurantWith = WithFavorited.<RestaurantDetailDto>builder()
                .restaurant(restaurant)
                .favorited(favorited)
                .build();
        ApiResult<WithFavorited<RestaurantDetailDto>> apiResult = ApiResult.success(restaurantWith);
        return ResponseEntity.ok(apiResult);
    }
}
