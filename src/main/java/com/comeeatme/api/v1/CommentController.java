package com.comeeatme.api.v1;

import com.comeeatme.api.common.dto.ApiResult;
import com.comeeatme.domain.comment.request.CommentCreate;
import com.comeeatme.domain.comment.request.CommentEdit;
import com.comeeatme.domain.comment.response.CommentDto;
import com.comeeatme.domain.comment.service.CommentService;
import com.comeeatme.domain.common.response.CreateResult;
import com.comeeatme.domain.common.response.UpdateResult;
import com.comeeatme.error.exception.EntityAccessDeniedException;
import com.comeeatme.error.exception.EntityNotFoundException;
import com.comeeatme.security.annotation.CurrentUsername;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequestMapping("/v1/posts/{postId}/comments")
@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<ApiResult<CreateResult<Long>>> post(
            @Valid @RequestBody CommentCreate commentCreate, @PathVariable Long postId,
            @CurrentUsername String username) {
        CreateResult<Long> createResult = commentService.create(commentCreate, username, postId);
        ApiResult<CreateResult<Long>> result = ApiResult.success(createResult);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<ApiResult<UpdateResult<Long>>> patch(
            @Valid @RequestBody CommentEdit commentEdit, @PathVariable Long postId, @PathVariable Long commentId,
            @CurrentUsername String username) {
        if (commentService.isNotOwnedByMember(commentId, username)) {
            throw new EntityAccessDeniedException(String.format("commentId=%s, username=%s", commentId, username));
        }
        if (commentService.isNotBelongToPost(commentId, postId)) {
            throw new EntityNotFoundException(String.format("commentId=%s, postId=%s", commentId, postId));
        }
        UpdateResult<Long> updateResult = commentService.edit(commentEdit, commentId);
        ApiResult<UpdateResult<Long>> result = ApiResult.success(updateResult);
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

    @GetMapping
    public ResponseEntity<ApiResult<Slice<CommentDto>>> get(@PathVariable Long postId, Pageable pageable) {
        Slice<CommentDto> commentDtos = commentService.getListOfPost(pageable, postId);
        ApiResult<Slice<CommentDto>> result = ApiResult.success(commentDtos);
        return ResponseEntity.ok(result);
    }
}
