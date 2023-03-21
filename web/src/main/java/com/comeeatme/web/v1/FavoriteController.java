package com.comeeatme.web.v1;

import com.comeeatme.api.account.AccountService;
import com.comeeatme.api.favorite.FavoriteService;
import com.comeeatme.api.favorite.response.FavoriteRestaurantDto;
import com.comeeatme.api.favorite.response.RestaurantFavorited;
import com.comeeatme.api.image.ImageService;
import com.comeeatme.api.post.PostHashtagService;
import com.comeeatme.domain.post.Hashtag;
import com.comeeatme.web.common.response.ApiResult;
import com.comeeatme.web.common.response.RestaurantWith;
import com.comeeatme.web.security.annotation.LoginUsername;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.*;
import java.util.stream.Collectors;

@RequestMapping("/v1")
@RestController
@RequiredArgsConstructor
public class FavoriteController {

    private final AccountService accountService;

    private final FavoriteService favoriteService;

    private final ImageService imageService;

    private final PostHashtagService postHashtagService;

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
            Pageable pageable, @PathVariable Long memberId,
            @RequestParam(required = false) @Valid @Max(10) @Min(1) Integer perImageNum,
            @LoginUsername String username) {
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
        Map<Long, List<String>> restaurantIdToImages = Optional.ofNullable(perImageNum)
                .map(num -> imageService.getRestaurantIdToImages(restaurantIds, num))
                .orElse(null);
        Map<Long, List<Hashtag>> restaurantIdToHashtags = postHashtagService.getHashtagsOfRestaurants(restaurantIds);
        Slice<RestaurantWith<FavoriteRestaurantDto>> restaurantWiths = restaurants
                .map(restaurant -> RestaurantWith.<FavoriteRestaurantDto>builder()
                        .restaurant(restaurant)
                        .favorited(favoriteRestaurantIds.contains(restaurant.getId()))
                        .hashtags(restaurantIdToHashtags.getOrDefault(restaurant.getId(), Collections.emptyList()))
                        .imageUrls(Optional.ofNullable(restaurantIdToImages)
                                .map(idToImages -> idToImages.getOrDefault(restaurant.getId(),
                                        Collections.emptyList()))
                                .orElse(null)
                        )
                        .build()
                );
        ApiResult<Slice<RestaurantWith<FavoriteRestaurantDto>>> apiResult = ApiResult.success(restaurantWiths);
        return ResponseEntity.ok(apiResult);
    }

}
