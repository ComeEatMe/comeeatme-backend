package com.comeeatme.web.v1;

import com.comeeatme.api.account.AccountService;
import com.comeeatme.api.favorite.FavoriteService;
import com.comeeatme.api.favorite.response.RestaurantFavorited;
import com.comeeatme.api.image.ImageService;
import com.comeeatme.api.post.PostHashtagService;
import com.comeeatme.api.restaurant.RestaurantService;
import com.comeeatme.api.restaurant.request.RestaurantSearch;
import com.comeeatme.api.restaurant.response.RestaurantDto;
import com.comeeatme.api.restaurant.response.RestaurantSimpleDto;
import com.comeeatme.domain.post.Hashtag;
import com.comeeatme.web.common.response.ApiResult;
import com.comeeatme.web.common.response.RestaurantWith;
import com.comeeatme.web.security.annotation.LoginUsername;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.*;
import java.util.stream.Collectors;

@RequestMapping("/v1")
@RestController
@RequiredArgsConstructor
public class RestaurantController {

    private final AccountService accountService;

    private final RestaurantService restaurantService;

    private final FavoriteService favoriteService;

    private final PostHashtagService postHashtagService;

    private final ImageService imageService;

    @GetMapping("/restaurants/simple")
    public ResponseEntity<ApiResult<Slice<RestaurantSimpleDto>>> searchSimple(
            Pageable pageable, @ModelAttribute RestaurantSearch restaurantSearch) {
        Slice<RestaurantSimpleDto> restaurants = restaurantService.search(pageable, restaurantSearch)
                .map(restaurant -> RestaurantSimpleDto.builder()
                        .id(restaurant.getId())
                        .name(restaurant.getName())
                        .addressName(StringUtils.hasText(restaurant.getAddress().getName()) ?
                                restaurant.getAddress().getName() : restaurant.getAddress().getRoadName())
                        .build()
                );
        ApiResult<Slice<RestaurantSimpleDto>> result = ApiResult.success(restaurants);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/restaurants")
    public ResponseEntity<ApiResult<Slice<RestaurantWith<RestaurantDto>>>> search(
            Pageable pageable, @ModelAttribute RestaurantSearch restaurantSearch, @LoginUsername String username) {
        Long memberId = accountService.getMemberId(username);
        Slice<RestaurantDto> restaurants = restaurantService.search(pageable, restaurantSearch);
        List<Long> restaurantIds = restaurants.stream()
                .map(RestaurantDto::getId)
                .collect(Collectors.toList());
        Set<Long> favoriteRestaurantIds = getFavoriteRestaurantIds(memberId, restaurantIds);
        Map<Long, List<Hashtag>> restaurantIdToHashtags = postHashtagService.getHashtagsOfRestaurants(restaurantIds);
        Map<Long, List<String>> restaurantIdToImages = Optional.ofNullable(restaurantSearch.getPerImageNum())
                .map(perImageNum -> imageService.getRestaurantIdToImages(restaurantIds, perImageNum))
                .orElse(null);
        Slice<RestaurantWith<RestaurantDto>> restaurantWiths = restaurants
                .map(restaurant -> RestaurantWith
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
        ApiResult<Slice<RestaurantWith<RestaurantDto>>> apiResult = ApiResult.success(restaurantWiths);
        return ResponseEntity.ok(apiResult);
    }

    @GetMapping("/restaurants/order")
    public ResponseEntity<ApiResult<Slice<RestaurantWith<RestaurantDto>>>> getRankedList(
            @LoginUsername String username, Pageable pageable,
            @RequestParam(required = false) String addressCode,
            @RequestParam(required = false) @Valid @Max(10) @Min(1) Integer perImageNum) {
        Long memberId = accountService.getMemberId(username);
        Slice<RestaurantDto> restaurants = restaurantService.getOrderedList(pageable, addressCode);
        List<Long> restaurantIds = restaurants.stream()
                .map(RestaurantDto::getId)
                .collect(Collectors.toList());
        Set<Long> favoriteRestaurantIds = getFavoriteRestaurantIds(memberId, restaurantIds);
        Map<Long, List<Hashtag>> restaurantIdToHashtags = postHashtagService.getHashtagsOfRestaurants(restaurantIds);
        Map<Long, List<String>> restaurantIdToImages = Optional.ofNullable(perImageNum)
                .map(num -> imageService.getRestaurantIdToImages(restaurantIds, num))
                .orElse(null);
        Slice<RestaurantWith<RestaurantDto>> restaurantWiths = restaurants
                .map(restaurant -> RestaurantWith
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
    public ResponseEntity<ApiResult<RestaurantWith<RestaurantDto>>> get(
            @PathVariable Long restaurantId, @LoginUsername String username) {
        Long memberId = accountService.getMemberId(username);
        RestaurantDto restaurant = restaurantService.get(restaurantId);
        boolean favorited = favoriteService.isFavorite(memberId, restaurant.getId());
        List<Hashtag> hashtags = postHashtagService.getHashtagsOfRestaurant(restaurant.getId());
        RestaurantWith<RestaurantDto> restaurantWith = RestaurantWith
                .restaurant(restaurant)
                .favorited(favorited)
                .hashtags(hashtags)
                .build();
        ApiResult<RestaurantWith<RestaurantDto>> apiResult = ApiResult.success(restaurantWith);
        return ResponseEntity.ok(apiResult);
    }

}
