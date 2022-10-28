package com.comeeatme.api.v1;

import com.comeeatme.api.common.response.ApiResult;
import com.comeeatme.domain.like.response.LikedResult;
import com.comeeatme.domain.like.service.LikeService;
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

    @PutMapping("/member/like/{postId}")
    public ResponseEntity<Void> like(@PathVariable Long postId, @CurrentUsername String username) {
        likeService.like(postId, username);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/member/like/{postId}")
    public ResponseEntity<Void> unlike(@PathVariable Long postId, @CurrentUsername String username) {
        likeService.unlike(postId, username);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/members/{memberId}/liked")
    public ResponseEntity<ApiResult<List<LikedResult>>> getLiked(
            @RequestParam @Valid @NotNull @Size(max = 100) List<Long> postIds, @PathVariable Long memberId) {
        List<LikedResult> likedResults = likeService.isLiked(postIds, memberId);
        ApiResult<List<LikedResult>> result = ApiResult.success(likedResults);
        return ResponseEntity.ok(result);
    }

}
