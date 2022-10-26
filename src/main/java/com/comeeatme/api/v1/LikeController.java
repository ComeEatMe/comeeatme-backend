package com.comeeatme.api.v1;

import com.comeeatme.api.common.dto.ApiResult;
import com.comeeatme.domain.likes.response.LikeResult;
import com.comeeatme.domain.likes.response.LikedResult;
import com.comeeatme.domain.likes.service.LikeService;
import com.comeeatme.security.annotation.CurrentUsername;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@RequestMapping("/v1")
@RestController
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PutMapping("/posts/{postId}/like")
    public ResponseEntity<ApiResult<LikeResult>> like(@PathVariable Long postId, @CurrentUsername String username) {
        LikeResult likeResult = likeService.pushLike(postId, username);
        ApiResult<LikeResult> result = ApiResult.success(likeResult);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/posts/liked")
    public ResponseEntity<ApiResult<List<LikedResult>>> getLiked(
            @RequestParam @Valid @NotNull @Size(max = 100) List<Long> postIds, @CurrentUsername String username) {
        List<LikedResult> likedResults = likeService.isLiked(postIds, username);
        ApiResult<List<LikedResult>> result = ApiResult.success(likedResults);
        return ResponseEntity.ok(result);
    }

}
