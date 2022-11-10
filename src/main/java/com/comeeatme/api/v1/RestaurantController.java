package com.comeeatme.api.v1;

import com.comeeatme.api.common.response.ApiResult;
import com.comeeatme.api.common.response.RestaurantWith;
import com.comeeatme.domain.account.service.AccountService;
import com.comeeatme.domain.favorite.response.RestaurantFavorited;
import com.comeeatme.domain.favorite.service.FavoriteService;
import com.comeeatme.domain.restaurant.request.RestaurantSearch;
import com.comeeatme.domain.restaurant.response.RestaurantDetailDto;
import com.comeeatme.domain.restaurant.response.RestaurantDto;
import com.comeeatme.domain.restaurant.response.RestaurantSimpleDto;
import com.comeeatme.domain.restaurant.service.RestaurantService;
import com.comeeatme.security.annotation.CurrentUsername;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    @GetMapping("/restaurants")
    public ResponseEntity<ApiResult<Slice<RestaurantWith<RestaurantDto>>>> getList(
            Pageable pageable, @ModelAttribute RestaurantSearch restaurantSearch, @CurrentUsername String username) {
        Long memberId = accountService.getMemberId(username);
        Slice<RestaurantDto> restaurants = restaurantService.getList(pageable, restaurantSearch);
        List<Long> restaurantIds = restaurants.stream()
                .map(RestaurantDto::getId)
                .collect(Collectors.toList());
        Set<Long> favoriteRestaurantIds = getFavoriteRestaurantIds(memberId, restaurantIds);
        Slice<RestaurantWith<RestaurantDto>> restaurantWiths = restaurants
                .map(restaurant -> RestaurantWith
                        .restaurant(restaurant)
                        .favorited(favoriteRestaurantIds.contains(restaurant.getId()))
                        .build()
                );
        ApiResult<Slice<RestaurantWith<RestaurantDto>>> apiResult = ApiResult.success(restaurantWiths);
        return ResponseEntity.ok(apiResult);
    }

    private Set<Long> getFavoriteRestaurantIds(Long memberId, List<Long> restaurantIds) {
        return favoriteService.areFavorite(memberId, restaurantIds)
                .stream()
                .filter(RestaurantFavorited::getFavorited)
                .map(RestaurantFavorited::getRestaurantId)
                .collect(Collectors.toSet());
    }

    @GetMapping("/restaurants/{restaurantId}")
    public ResponseEntity<ApiResult<RestaurantWith<RestaurantDetailDto>>> get(
            @PathVariable Long restaurantId, @CurrentUsername String username) {
        Long memberId = accountService.getMemberId(username);
        RestaurantDetailDto restaurant = restaurantService.get(restaurantId);
        boolean favorited = favoriteService.isFavorite(memberId, restaurant.getId());
        RestaurantWith<RestaurantDetailDto> restaurantWith = RestaurantWith.<RestaurantDetailDto>builder()
                .restaurant(restaurant)
                .favorited(favorited)
                .build();
        ApiResult<RestaurantWith<RestaurantDetailDto>> apiResult = ApiResult.success(restaurantWith);
        return ResponseEntity.ok(apiResult);
    }

}
