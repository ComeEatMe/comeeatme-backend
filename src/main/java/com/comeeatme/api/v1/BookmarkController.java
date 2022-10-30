package com.comeeatme.api.v1;

import com.comeeatme.api.common.response.ApiResult;
import com.comeeatme.api.common.response.WithLikedBookmarked;
import com.comeeatme.domain.account.service.AccountService;
import com.comeeatme.domain.bookmark.response.BookmarkGroupDto;
import com.comeeatme.domain.bookmark.response.BookmarkedPostDto;
import com.comeeatme.domain.bookmark.response.PostBookmarked;
import com.comeeatme.domain.bookmark.service.BookmarkService;
import com.comeeatme.domain.like.response.PostLiked;
import com.comeeatme.domain.like.service.LikeService;
import com.comeeatme.security.annotation.CurrentUsername;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RequestMapping("/v1")
@RestController
@RequiredArgsConstructor
public class BookmarkController {

    private final AccountService accountService;

    private final BookmarkService bookmarkService;

    private final LikeService likeService;

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

    @GetMapping({"/members/{memberId}/bookmarked/{groupName}", "/members/{memberId}/bookmarked"})
    public ResponseEntity<ApiResult<Slice<WithLikedBookmarked<BookmarkedPostDto>>>> getBookmarkedList(
            Pageable pageable, @PathVariable Long memberId, @PathVariable(required = false) String groupName,
            @CurrentUsername String username) {
        Long myMemberId = accountService.getMemberId(username);
        Slice<BookmarkedPostDto> posts = bookmarkService.getBookmarkedPosts(pageable, memberId, groupName);
        List<Long> postIds = posts.stream()
                .map(BookmarkedPostDto::getId)
                .collect(Collectors.toList());
        Set<Long> likedPostIds = likeService.areLiked(myMemberId, postIds).stream()
                .filter(PostLiked::getLiked)
                .map(PostLiked::getPostId)
                .collect(Collectors.toSet());
        Set<Long> bookmarkedPostIds = Objects.equals(myMemberId, memberId) ?
                new HashSet<>(postIds) :
                bookmarkService.isBookmarked(myMemberId, postIds).stream()
                        .filter(PostBookmarked::getBookmarked)
                        .map(PostBookmarked::getPostId)
                        .collect(Collectors.toSet());
        Slice<WithLikedBookmarked<BookmarkedPostDto>> postWiths = posts
                .map(post -> WithLikedBookmarked.<BookmarkedPostDto>builder()
                        .post(post)
                        .liked(likedPostIds.contains(post.getId()))
                        .bookmarked(bookmarkedPostIds.contains(post.getId()))
                        .build()
                );
        ApiResult<Slice<WithLikedBookmarked<BookmarkedPostDto>>> apiResult = ApiResult.success(postWiths);
        return ResponseEntity.ok(apiResult);
    }

}
