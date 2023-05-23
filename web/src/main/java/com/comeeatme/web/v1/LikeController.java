package com.comeeatme.web.v1;

import com.comeeatme.api.account.AccountService;
import com.comeeatme.api.bookmark.BookmarkService;
import com.comeeatme.api.bookmark.response.PostBookmarked;
import com.comeeatme.api.like.LikeService;
import com.comeeatme.api.like.response.LikedPostDto;
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
public class LikeController {

    private final LikeService likeService;

    private final AccountService accountService;

    private final BookmarkService bookmarkService;

    @PutMapping("/member/like/{postId}")
    public ResponseEntity<ApiResult<Void>> like(@PathVariable Long postId, @LoginUsername String username) {
        Long memberId = accountService.getMemberId(username);
        likeService.like(postId, memberId);
        return ResponseEntity.ok(ApiResult.success());
    }

    @DeleteMapping("/member/like/{postId}")
    public ResponseEntity<ApiResult<Void>> unlike(@PathVariable Long postId, @LoginUsername String username) {
        Long memberId = accountService.getMemberId(username);
        likeService.unlike(postId, memberId);
        return ResponseEntity.ok(ApiResult.success());
    }

    @GetMapping("/members/{memberId}/liked")
    public ResponseEntity<ApiResult<Slice<PostWith<LikedPostDto>>>> getLikedList(
            Pageable pageable, @PathVariable Long memberId, @LoginUsername String username) {
        Long myMemberId = accountService.getMemberId(username);
        Slice<LikedPostDto> posts = likeService.getLikedPosts(pageable, memberId);
        List<Long> postIds = posts.stream()
                .map(LikedPostDto::getId)
                .collect(Collectors.toList());
        Set<Long> likedPostIds = Objects.equals(myMemberId, memberId) ?
                new HashSet<>(postIds) :
                likeService.areLiked(myMemberId, postIds).stream()
                        .filter(PostLiked::getLiked)
                        .map(PostLiked::getPostId)
                        .collect(Collectors.toSet());
        Set<Long> bookmarkedPostIds = bookmarkService.areBookmarked(myMemberId, postIds).stream()
                .filter(PostBookmarked::getBookmarked)
                .map(PostBookmarked::getPostId)
                .collect(Collectors.toSet());
        Slice<PostWith<LikedPostDto>> postWiths = posts
                .map(post -> PostWith.<LikedPostDto>builder()
                        .post(post)
                        .liked(likedPostIds.contains(post.getId()))
                        .bookmarked(bookmarkedPostIds.contains(post.getId()))
                        .build()
                );
        ApiResult<Slice<PostWith<LikedPostDto>>> apiResult = ApiResult.success(postWiths);
        return ResponseEntity.ok(apiResult);
    }
}
