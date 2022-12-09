package com.comeeatme.api.v1;

import com.comeeatme.api.common.response.ApiResult;
import com.comeeatme.api.common.response.RestaurantWith;
import com.comeeatme.domain.account.service.AccountService;
import com.comeeatme.domain.favorite.response.FavoriteRestaurantDto;
import com.comeeatme.domain.favorite.response.RestaurantFavorited;
import com.comeeatme.domain.favorite.service.FavoriteService;
import com.comeeatme.security.annotation.LoginUsername;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RequestMapping("/v1")
@RestController
@RequiredArgsConstructor
public class FavoriteController {

    private final AccountService accountService;

    private final FavoriteService favoriteService;

    @PutMapping("/member/favorite/{restaurantId}")
    public ResponseEntity<ApiResult<Void>> put(
            @PathVariable Long restaurantId, @LoginUsername String username) {
        Long memberId = accountService.getMemberId(username);
        favoriteService.favorite(restaurantId, memberId);
        return ResponseEntity.ok(ApiResult.success());
    }

    @DeleteMapping("/member/favorite/{restaurantId}")
    public ResponseEntity<ApiResult<Void>> delete(
            @PathVariable Long restaurantId, @LoginUsername String username) {
        Long memberId = accountService.getMemberId(username);
        favoriteService.cancelFavorite(restaurantId, memberId);
        return ResponseEntity.ok(ApiResult.success());
    }

    @GetMapping( "/members/{memberId}/favorite")
    public ResponseEntity<ApiResult<Slice<RestaurantWith<FavoriteRestaurantDto>>>> getFavoriteList(
            Pageable pageable, @PathVariable Long memberId, @LoginUsername String username) {
        Long myMemberId = accountService.getMemberId(username);
        Slice<FavoriteRestaurantDto> restaurants = favoriteService.getFavoriteRestaurants(pageable, memberId);
        List<Long> restaurantIds = restaurants.stream()
                .map(FavoriteRestaurantDto::getId)
                .collect(Collectors.toList());
        Set<Long> favoriteRestaurantIds = Objects.equals(myMemberId, memberId) ?
                new HashSet<>(restaurantIds) :
                favoriteService.areFavorite(myMemberId, restaurantIds).stream()
                        .filter(RestaurantFavorited::getFavorited)
                        .map(RestaurantFavorited::getRestaurantId)
                        .collect(Collectors.toSet());
        Slice<RestaurantWith<FavoriteRestaurantDto>> restaurantWiths = restaurants
                .map(restaurant -> RestaurantWith.<FavoriteRestaurantDto>builder()
                        .restaurant(restaurant)
                        .favorited(favoriteRestaurantIds.contains(restaurant.getId()))
                        .build()
                );
        ApiResult<Slice<RestaurantWith<FavoriteRestaurantDto>>> apiResult = ApiResult.success(restaurantWiths);
        return ResponseEntity.ok(apiResult);
    }

}
