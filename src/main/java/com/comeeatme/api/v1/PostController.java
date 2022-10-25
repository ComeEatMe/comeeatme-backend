package com.comeeatme.api.v1;

import com.comeeatme.api.common.dto.ApiResult;
import com.comeeatme.domain.common.response.CreateResult;
import com.comeeatme.domain.common.response.UpdateResult;
import com.comeeatme.domain.images.service.ImageService;
import com.comeeatme.domain.likes.response.LikeResult;
import com.comeeatme.domain.likes.response.LikedResult;
import com.comeeatme.domain.likes.service.LikeService;
import com.comeeatme.domain.post.request.PostCreate;
import com.comeeatme.domain.post.request.PostEdit;
import com.comeeatme.domain.post.request.PostSearch;
import com.comeeatme.domain.post.response.PostDto;
import com.comeeatme.domain.post.service.PostService;
import com.comeeatme.error.exception.EntityAccessDeniedException;
import com.comeeatme.error.exception.InvalidImageIdception;
import com.comeeatme.security.annotation.CurrentUsername;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@RequestMapping("/v1/posts")
@RestController
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    private final ImageService imageService;

    private final LikeService likeService;

    @GetMapping
    public ResponseEntity<ApiResult<Slice<PostDto>>> getList(
            Pageable pageable, @ModelAttribute PostSearch postSearch) {
        Slice<PostDto> posts = postService.getList(pageable, postSearch);
        ApiResult<Slice<PostDto>> result = ApiResult.success(posts);
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<ApiResult<CreateResult<Long>>> post(
            @Valid @RequestBody PostCreate postCreate, @CurrentUsername String username) {
        if (!imageService.validateImageIds(postCreate.getImageIds(), username)) {
            throw new InvalidImageIdception("imageIds=" + postCreate.getImageIds());
        }
        CreateResult<Long> createResult = postService.create(postCreate, username);
        ApiResult<CreateResult<Long>> result = ApiResult.success(createResult);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<ApiResult<UpdateResult<Long>>> patch(
            @Valid @RequestBody PostEdit postEdit, @PathVariable Long postId, @CurrentUsername String username) {
        if (postService.isNotOwnedByMember(postId, username)) {
            throw new EntityAccessDeniedException(String.format("postId=%s, username=%s", postId, username));
        }
        UpdateResult<Long> updateResult = postService.edit(postEdit, postId);
        ApiResult<UpdateResult<Long>> result = ApiResult.success(updateResult);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResult<Long>> delete(@PathVariable Long postId, @CurrentUsername String username) {
        if (postService.isNotOwnedByMember(postId, username)) {
            throw new EntityAccessDeniedException(String.format("postId=%s, username=%s", postId, username));
        }
        Long deletedId = postService.delete(postId);
        ApiResult<Long> result = ApiResult.success(deletedId);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{postId}/like")
    public ResponseEntity<ApiResult<LikeResult>> like(@PathVariable Long postId, @CurrentUsername String username) {
        LikeResult likeResult = likeService.pushLike(postId, username);
        ApiResult<LikeResult> result = ApiResult.success(likeResult);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/liked")
    public ResponseEntity<ApiResult<List<LikedResult>>> getLiked(
            @RequestParam @Valid @NotNull @Size(max = 100) List<Long> postIds, @CurrentUsername String username) {
        List<LikedResult> likedResults = likeService.isLiked(postIds, username);
        ApiResult<List<LikedResult>> result = ApiResult.success(likedResults);
        return ResponseEntity.ok(result);
    }

}
