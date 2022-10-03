package com.comeeatme.api.v1.posts.comments;

import com.comeeatme.api.common.dto.ApiResult;
import com.comeeatme.domain.comment.request.CommentCreate;
import com.comeeatme.domain.comment.service.CommentService;
import com.comeeatme.security.annotation.CurrentUsername;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequestMapping("/v1/posts/{postId}/comments")
@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<ApiResult<Long>> post(
            @Valid @RequestBody CommentCreate commentCreate, @PathVariable Long postId,
            @CurrentUsername String username) {
        Long commentId = commentService.create(commentCreate, username, postId);
        ApiResult<Long> result = ApiResult.success(commentId);
        return ResponseEntity.ok(result);
    }
}
