package com.comeeatme.api.v1;

import com.comeeatme.api.common.response.ApiResult;
import com.comeeatme.domain.bookmark.response.BookmarkGroupDto;
import com.comeeatme.domain.bookmark.service.BookmarkService;
import com.comeeatme.security.annotation.CurrentUsername;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/v1")
@RestController
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @PutMapping({"/member/bookmark/{groupName}/{postId}", "/member/bookmark/{postId}"})
    public ResponseEntity<Void> bookmark(
            @PathVariable(required = false) String groupName, @PathVariable Long postId,
            @CurrentUsername String username) {
        bookmarkService.bookmark(postId, username, groupName);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping({"/member/bookmark/{groupName}/{postId}", "/member/bookmark/{postId}"})
    public ResponseEntity<Void> delete(
            @PathVariable(required = false) String groupName, @PathVariable Long postId,
            @CurrentUsername String username) {
        bookmarkService.cancelBookmark(postId, username, groupName);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/members/{memberId}/bookmark-groups")
    public ResponseEntity<ApiResult<List<BookmarkGroupDto>>> getBookmarkGroups(@PathVariable Long memberId) {
        List<BookmarkGroupDto> groups = bookmarkService.getAllGroupsOfMember(memberId);
        ApiResult<List<BookmarkGroupDto>> apiResult = ApiResult.success(groups);
        return ResponseEntity.ok(apiResult);
    }
}
