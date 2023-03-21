package com.comeeatme.web.v1;

import com.comeeatme.api.account.AccountService;
import com.comeeatme.api.bookmark.BookmarkService;
import com.comeeatme.api.bookmark.response.BookmarkedPostDto;
import com.comeeatme.api.bookmark.response.PostBookmarked;
import com.comeeatme.api.like.LikeService;
import com.comeeatme.api.like.response.PostLiked;
import com.comeeatme.web.common.response.ApiResult;
import com.comeeatme.web.common.response.PostWith;
import com.comeeatme.web.security.annotation.LoginUsername;
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

    @PutMapping("/member/bookmark/{postId}")
    public ResponseEntity<ApiResult<Void>> bookmark(
            @PathVariable Long postId, @LoginUsername String username) {
        Long memberId = accountService.getMemberId(username);
        bookmarkService.bookmark(postId, memberId);
        return ResponseEntity.ok(ApiResult.success());
    }

    @DeleteMapping("/member/bookmark/{postId}")
    public ResponseEntity<ApiResult<Void>> delete(
            @PathVariable Long postId, @LoginUsername String username) {
        Long memberId = accountService.getMemberId(username);
        bookmarkService.cancelBookmark(postId, memberId);
        return ResponseEntity.ok(ApiResult.success());
    }

    @GetMapping( "/members/{memberId}/bookmarked")
    public ResponseEntity<ApiResult<Slice<PostWith<BookmarkedPostDto>>>> getBookmarkedList(
            Pageable pageable, @PathVariable Long memberId, @LoginUsername String username) {
        Long myMemberId = accountService.getMemberId(username);
        Slice<BookmarkedPostDto> posts = bookmarkService.getBookmarkedPosts(pageable, memberId);
        List<Long> postIds = posts.stream()
                .map(BookmarkedPostDto::getId)
                .collect(Collectors.toList());
        Set<Long> likedPostIds = likeService.areLiked(myMemberId, postIds).stream()
                .filter(PostLiked::getLiked)
                .map(PostLiked::getPostId)
                .collect(Collectors.toSet());
        Set<Long> bookmarkedPostIds = Objects.equals(myMemberId, memberId) ?
                new HashSet<>(postIds) :
                bookmarkService.areBookmarked(myMemberId, postIds).stream()
                        .filter(PostBookmarked::getBookmarked)
                        .map(PostBookmarked::getPostId)
                        .collect(Collectors.toSet());
        Slice<PostWith<BookmarkedPostDto>> postWiths = posts
                .map(post -> PostWith.<BookmarkedPostDto>builder()
                        .post(post)
                        .liked(likedPostIds.contains(post.getId()))
                        .bookmarked(bookmarkedPostIds.contains(post.getId()))
                        .build()
                );
        ApiResult<Slice<PostWith<BookmarkedPostDto>>> apiResult = ApiResult.success(postWiths);
        return ResponseEntity.ok(apiResult);
    }

}
