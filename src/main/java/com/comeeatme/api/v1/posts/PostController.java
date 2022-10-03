package com.comeeatme.api.v1.posts;

import com.comeeatme.api.common.dto.ApiResult;
import com.comeeatme.domain.images.service.ImageService;
import com.comeeatme.domain.post.request.PostCreate;
import com.comeeatme.domain.post.service.PostService;
import com.comeeatme.error.exception.InvalidImageIdception;
import com.comeeatme.security.annotation.CurrentUsername;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RequestMapping("/v1/posts")
@RestController
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    private final ImageService imageService;

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
}
