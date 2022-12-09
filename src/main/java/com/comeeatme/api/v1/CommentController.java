package com.comeeatme.api.v1;

import com.comeeatme.api.common.response.ApiResult;
import com.comeeatme.security.account.service.AccountService;
import com.comeeatme.domain.comment.request.CommentCreate;
import com.comeeatme.domain.comment.request.CommentEdit;
import com.comeeatme.domain.comment.response.CommentDto;
import com.comeeatme.domain.comment.service.CommentService;
import com.comeeatme.domain.common.response.CreateResult;
import com.comeeatme.domain.common.response.DeleteResult;
import com.comeeatme.domain.common.response.UpdateResult;
import com.comeeatme.error.exception.EntityAccessDeniedException;
import com.comeeatme.error.exception.EntityNotFoundException;
import com.comeeatme.security.annotation.LoginUsername;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequestMapping("/v1")
@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    private final AccountService accountService;

    @PostMapping("/posts/{postId}/comment")
    public ResponseEntity<ApiResult<CreateResult<Long>>> post(
            @Valid @RequestBody CommentCreate commentCreate, @PathVariable Long postId,
            @LoginUsername String username) {
        Long memberId = accountService.getMemberId(username);
        CreateResult<Long> createResult = commentService.create(commentCreate, memberId, postId);
        ApiResult<CreateResult<Long>> result = ApiResult.success(createResult);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/posts/{postId}/comments/{commentId}")
    public ResponseEntity<ApiResult<UpdateResult<Long>>> patch(
            @Valid @RequestBody CommentEdit commentEdit, @PathVariable Long postId, @PathVariable Long commentId,
            @LoginUsername String username) {
        Long memberId = accountService.getMemberId(username);
        if (commentService.isNotOwnedByMember(commentId, memberId)) {
            throw new EntityAccessDeniedException(String.format("commentId=%s, username=%s", commentId, username));
        }
        if (commentService.isNotBelongToPost(commentId, postId)) {
            throw new EntityNotFoundException(String.format("commentId=%s, postId=%s", commentId, postId));
        }
        UpdateResult<Long> updateResult = commentService.edit(commentEdit, commentId);
        ApiResult<UpdateResult<Long>> result = ApiResult.success(updateResult);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    public ResponseEntity<ApiResult<DeleteResult<Long>>> delete(
            @PathVariable Long postId, @PathVariable Long commentId, @LoginUsername String username) {
        Long memberId = accountService.getMemberId(username);
        if (commentService.isNotOwnedByMember(commentId, memberId)) {
            throw new EntityAccessDeniedException(String.format("commentId=%s, username=%s", commentId, username));
        }
        if (commentService.isNotBelongToPost(commentId, postId)) {
            throw new EntityNotFoundException(String.format("commentId=%s, postId=%s", commentId, postId));
        }
        DeleteResult<Long> deleteResult = commentService.delete(commentId);
        ApiResult<DeleteResult<Long>> result = ApiResult.success(deleteResult);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResult<Slice<CommentDto>>> get(@PathVariable Long postId, Pageable pageable) {
        Slice<CommentDto> commentDtos = commentService.getListOfPost(pageable, postId);
        ApiResult<Slice<CommentDto>> result = ApiResult.success(commentDtos);
        return ResponseEntity.ok(result);
    }
}
