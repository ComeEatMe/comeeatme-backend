package com.comeeatme.api.v1;

import com.comeeatme.api.common.response.ApiResult;
import com.comeeatme.domain.favorite.response.FavoriteGroupDto;
import com.comeeatme.domain.favorite.service.FavoriteService;
import com.comeeatme.security.annotation.CurrentUsername;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/v1")
@RestController
@RequiredArgsConstructor
public class FavoriteController {

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

}
