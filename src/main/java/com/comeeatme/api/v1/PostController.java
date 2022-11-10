package com.comeeatme.api.v1;

import com.comeeatme.api.common.response.ApiResult;
import com.comeeatme.api.common.response.PostWith;
import com.comeeatme.domain.account.service.AccountService;
import com.comeeatme.domain.bookmark.response.PostBookmarked;
import com.comeeatme.domain.bookmark.service.BookmarkService;
import com.comeeatme.domain.common.response.CreateResult;
import com.comeeatme.domain.common.response.DeleteResult;
import com.comeeatme.domain.common.response.UpdateResult;
import com.comeeatme.domain.image.service.ImageService;
import com.comeeatme.domain.like.response.PostLiked;
import com.comeeatme.domain.like.service.LikeService;
import com.comeeatme.domain.post.request.PostCreate;
import com.comeeatme.domain.post.request.PostEdit;
import com.comeeatme.domain.post.request.PostSearch;
import com.comeeatme.domain.post.response.MemberPostDto;
import com.comeeatme.domain.post.response.PostDetailDto;
import com.comeeatme.domain.post.response.PostDto;
import com.comeeatme.domain.post.response.RestaurantPostDto;
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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequestMapping("/v1")
@RestController
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    private final ImageService imageService;

    private final LikeService likeService;

    private final BookmarkService bookmarkService;

    private final AccountService accountService;

    @GetMapping("/posts")
    public ResponseEntity<ApiResult<Slice<PostWith<PostDto>>>> getList(
            Pageable pageable, @ModelAttribute PostSearch postSearch, @CurrentUsername String username) {
        Long memberId = accountService.getMemberId(username);
        Slice<PostDto> posts = postService.getList(pageable, postSearch);
        List<Long> postIds = posts.stream()
                .map(PostDto::getId)
                .collect(Collectors.toList());
        Set<Long> likedPostIds = getLikedPostIds(memberId, postIds);
        Set<Long> bookmarkedPostIds = getBookmarkedPostIds(memberId, postIds);
        Slice<PostWith<PostDto>> postWiths = posts
                .map(post -> PostWith.<PostDto>builder()
                        .post(post)
                        .liked(likedPostIds.contains(post.getId()))
                        .bookmarked(bookmarkedPostIds.contains(post.getId()))
                        .build()
                );
        ApiResult<Slice<PostWith<PostDto>>> apiResult = ApiResult.success(postWiths);
        return ResponseEntity.ok(apiResult);
    }

    @GetMapping("/members/{memberId}/posts")
    public ResponseEntity<ApiResult<Slice<PostWith<MemberPostDto>>>> getListOfMember(
            Pageable pageable, @PathVariable Long memberId, @CurrentUsername String username) {
        Long myMemberId = accountService.getMemberId(username);
        Slice<MemberPostDto> posts = postService.getListOfMember(pageable, memberId);
        List<Long> postIds = posts.stream()
                .map(MemberPostDto::getId)
                .collect(Collectors.toList());
        Set<Long> likedPostIds = getLikedPostIds(myMemberId, postIds);
        Set<Long> bookmarkedPostIds = getBookmarkedPostIds(myMemberId, postIds);
        Slice<PostWith<MemberPostDto>> postWiths = posts
                .map(post -> PostWith.<MemberPostDto>builder()
                        .post(post)
                        .liked(likedPostIds.contains(post.getId()))
                        .bookmarked(bookmarkedPostIds.contains(post.getId()))
                        .build()
                );
        ApiResult<Slice<PostWith<MemberPostDto>>> apiResult = ApiResult.success(postWiths);
        return ResponseEntity.ok(apiResult);
    }

    @GetMapping("/restaurants/{restaurantId}/posts")
    public ResponseEntity<ApiResult<Slice<RestaurantPostDto>>> getListOfRestaurant(
            Pageable pageable, @PathVariable Long restaurantId, @CurrentUsername String username) {
        Slice<RestaurantPostDto> posts = postService.getListOfRestaurant(pageable, restaurantId);
        ApiResult<Slice<RestaurantPostDto>> apiResult = ApiResult.success(posts);
        return ResponseEntity.ok(apiResult);
    }

    private Set<Long> getLikedPostIds(Long memberId, List<Long> postIds) {
        return likeService.areLiked(memberId, postIds).stream()
                .filter(PostLiked::getLiked)
                .map(PostLiked::getPostId)
                .collect(Collectors.toSet());
    }

    private Set<Long> getBookmarkedPostIds(Long memberId, List<Long> postIds) {
        return bookmarkService.areBookmarked(memberId, postIds).stream()
                .filter(PostBookmarked::getBookmarked)
                .map(PostBookmarked::getPostId)
                .collect(Collectors.toSet());
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<ApiResult<PostWith<PostDetailDto>>> get(@PathVariable Long postId, @CurrentUsername String username) {
        Long memberId = accountService.getMemberId(username);
        PostDetailDto post = postService.get(postId);
        boolean bookmarked = bookmarkService.isBookmarked(memberId, postId);
        boolean liked = likeService.isLiked(memberId, postId);
        PostWith<PostDetailDto> postWith = PostWith.<PostDetailDto>builder()
                .post(post)
                .bookmarked(bookmarked)
                .liked(liked)
                .build();
        ApiResult<PostWith<PostDetailDto>> apiResult = ApiResult.success(postWith);
        return ResponseEntity.ok(apiResult);
    }

    @PostMapping("/post")
    public ResponseEntity<ApiResult<CreateResult<Long>>> post(
            @Valid @RequestBody PostCreate postCreate, @CurrentUsername String username) {
        if (!imageService.validateImageIds(postCreate.getImageIds(), username)) {
            throw new InvalidImageIdception("imageIds=" + postCreate.getImageIds());
        }
        CreateResult<Long> createResult = postService.create(postCreate, username);
        ApiResult<CreateResult<Long>> result = ApiResult.success(createResult);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/posts/{postId}")
    public ResponseEntity<ApiResult<UpdateResult<Long>>> patch(
            @Valid @RequestBody PostEdit postEdit, @PathVariable Long postId, @CurrentUsername String username) {
        if (postService.isNotOwnedByMember(postId, username)) {
            throw new EntityAccessDeniedException(String.format("postId=%s, username=%s", postId, username));
        }
        UpdateResult<Long> updateResult = postService.edit(postEdit, postId);
        ApiResult<UpdateResult<Long>> result = ApiResult.success(updateResult);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<ApiResult<DeleteResult<Long>>> delete(@PathVariable Long postId, @CurrentUsername String username) {
        if (postService.isNotOwnedByMember(postId, username)) {
            throw new EntityAccessDeniedException(String.format("postId=%s, username=%s", postId, username));
        }
        DeleteResult<Long> deleteResult = postService.delete(postId);
        ApiResult<DeleteResult<Long>> result = ApiResult.success(deleteResult);
        return ResponseEntity.ok(result);
    }

}
