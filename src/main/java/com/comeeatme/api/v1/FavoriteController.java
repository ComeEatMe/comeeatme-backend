package com.comeeatme.api.v1;

import com.comeeatme.api.common.response.ApiResult;
import com.comeeatme.api.common.response.WithFavorited;
import com.comeeatme.domain.account.service.AccountService;
import com.comeeatme.domain.favorite.response.FavoriteGroupDto;
import com.comeeatme.domain.favorite.response.FavoriteRestaurantDto;
import com.comeeatme.domain.favorite.response.RestaurantFavorited;
import com.comeeatme.domain.favorite.service.FavoriteService;
import com.comeeatme.security.annotation.CurrentUsername;
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

    @PutMapping({"/member/favorite/{groupName}/{restaurantId}", "/member/favorite/{restaurantId}"})
    public ResponseEntity<Void> put(
            @PathVariable(required = false) String groupName, @PathVariable Long restaurantId,
            @CurrentUsername String username) {
        favoriteService.favorite(restaurantId, username, groupName);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping({"/member/favorite/{groupName}/{restaurantId}", "/member/favorite/{restaurantId}"})
    public ResponseEntity<Void> delete(
            @PathVariable(required = false) String groupName, @PathVariable Long restaurantId,
            @CurrentUsername String username) {
        favoriteService.cancelFavorite(restaurantId, username, groupName);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/members/{memberId}/favorite-groups")
    public ResponseEntity<ApiResult<List<FavoriteGroupDto>>> getFavoriteGroups(@PathVariable Long memberId) {
        List<FavoriteGroupDto> groups = favoriteService.getAllGroupsOfMember(memberId);
        ApiResult<List<FavoriteGroupDto>> apiResult = ApiResult.success(groups);
        return ResponseEntity.ok(apiResult);
    }

    @GetMapping({"/members/{memberId}/favorite/{groupName}", "/members/{memberId}/favorite"})
    public ResponseEntity<ApiResult<Slice<WithFavorited<FavoriteRestaurantDto>>>> getFavoriteList(
            Pageable pageable, @PathVariable Long memberId, @PathVariable(required = false) String groupName,
            @CurrentUsername String username) {
        Long myMemberId = accountService.getMemberId(username);
        Slice<FavoriteRestaurantDto> restaurants = favoriteService.getFavoriteRestaurants(
                pageable, memberId, groupName);
        List<Long> restaurantIds = restaurants.stream()
                .map(FavoriteRestaurantDto::getId)
                .collect(Collectors.toList());
        Set<Long> favoriteRestaurantIds = Objects.equals(myMemberId, memberId) ?
                new HashSet<>(restaurantIds) :
                favoriteService.areFavorite(myMemberId, restaurantIds).stream()
                        .filter(RestaurantFavorited::getFavorited)
                        .map(RestaurantFavorited::getRestaurantId)
                        .collect(Collectors.toSet());
        Slice<WithFavorited<FavoriteRestaurantDto>> restaurantWiths = restaurants
                .map(restaurant -> WithFavorited.<FavoriteRestaurantDto>builder()
                        .restaurant(restaurant)
                        .favorited(favoriteRestaurantIds.contains(restaurant.getId()))
                        .build()
                );
        ApiResult<Slice<WithFavorited<FavoriteRestaurantDto>>> apiResult = ApiResult.success(restaurantWiths);
        return ResponseEntity.ok(apiResult);
    }

}
