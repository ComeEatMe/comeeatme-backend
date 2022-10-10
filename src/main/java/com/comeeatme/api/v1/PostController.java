package com.comeeatme.api.v1;

import com.comeeatme.api.common.dto.ApiResult;
import com.comeeatme.domain.images.service.ImageService;
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
    public ResponseEntity<ApiResult<Long>> post(
            @Valid @RequestBody PostCreate postCreate, @CurrentUsername String username) {
        if (!imageService.validateImageIds(postCreate.getImageIds(), username)) {
            throw new InvalidImageIdception("imageIds=" + postCreate.getImageIds());
        }
        long postId = postService.create(postCreate, username);
        ApiResult<Long> result = ApiResult.success(postId);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<ApiResult<Long>> patch(
            @Valid @RequestBody PostEdit postEdit, @PathVariable Long postId, @CurrentUsername String username) {
        if (postService.isNotOwnedByMember(postId, username)) {
            throw new EntityAccessDeniedException(String.format("postId=%s, username=%s", postId, username));
        }
        Long editedPostId = postService.edit(postEdit, postId);
        ApiResult<Long> result = ApiResult.success(editedPostId);
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
    public ResponseEntity<ApiResult<Boolean>> like(@PathVariable Long postId, @CurrentUsername String username) {
        boolean created = likeService.pushLike(postId, username);
        ApiResult<Boolean> result = ApiResult.success(created);
        return ResponseEntity.ok(result);
    }

}
