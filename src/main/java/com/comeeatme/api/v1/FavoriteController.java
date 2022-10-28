package com.comeeatme.api.v1;

import com.comeeatme.domain.favorite.service.FavoriteService;
import com.comeeatme.security.annotation.CurrentUsername;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

}
