package com.comeeatme.api.v1.posts.comments;

import com.comeeatme.api.common.dto.ApiResult;
import com.comeeatme.domain.comment.request.CommentCreate;
import com.comeeatme.domain.comment.request.CommentEdit;
import com.comeeatme.domain.comment.service.CommentService;
import com.comeeatme.error.exception.EntityAccessDeniedException;
import com.comeeatme.error.exception.EntityNotFoundException;
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

    @PatchMapping("/{commentId}")
    public ResponseEntity<ApiResult<Long>> patch(
            @Valid @RequestBody CommentEdit commentEdit, @PathVariable Long postId, @PathVariable Long commentId,
            @CurrentUsername String username) {
        if (commentService.isNotOwnedByMember(commentId, username)) {
            throw new EntityAccessDeniedException(String.format("commentId=%s, username=%s", commentId, username));
        }
        if (commentService.isNotBelongToPost(commentId, postId)) {
            throw new EntityNotFoundException(String.format("commentId=%s, postId=%s", commentId, postId));
        }
        Long editedCommentId = commentService.edit(commentEdit, commentId);
        ApiResult<Long> result = ApiResult.success(editedCommentId);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResult<Long>> delete(
            @PathVariable Long postId, @PathVariable Long commentId, @CurrentUsername String username) {
        if (commentService.isNotOwnedByMember(commentId, username)) {
            throw new EntityAccessDeniedException(String.format("commentId=%s, username=%s", commentId, username));
        }
        if (commentService.isNotBelongToPost(commentId, postId)) {
            throw new EntityNotFoundException(String.format("commentId=%s, postId=%s", commentId, postId));
        }
        Long deletedCommentId = commentService.delete(commentId);
        ApiResult<Long> result = ApiResult.success(deletedCommentId);
        return ResponseEntity.ok(result);
    }
}
