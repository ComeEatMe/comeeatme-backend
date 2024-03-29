package com.comeeatme.web.v1;

import com.comeeatme.api.account.AccountService;
import com.comeeatme.api.bookmark.BookmarkService;
import com.comeeatme.api.bookmark.response.PostBookmarked;
import com.comeeatme.api.common.response.CreateResult;
import com.comeeatme.api.common.response.DeleteResult;
import com.comeeatme.api.common.response.UpdateResult;
import com.comeeatme.api.exception.EntityAccessDeniedException;
import com.comeeatme.api.exception.InvalidImageIdception;
import com.comeeatme.api.image.ImageService;
import com.comeeatme.api.like.LikeService;
import com.comeeatme.api.like.response.PostLiked;
import com.comeeatme.api.post.PostService;
import com.comeeatme.api.post.request.PostCreate;
import com.comeeatme.api.post.request.PostEdit;
import com.comeeatme.api.post.response.MemberPostDto;
import com.comeeatme.api.post.response.PostDetailDto;
import com.comeeatme.api.post.response.PostDto;
import com.comeeatme.api.post.response.RestaurantPostDto;
import com.comeeatme.domain.post.request.PostSearch;
import com.comeeatme.web.common.response.ApiResult;
import com.comeeatme.web.common.response.PostWith;
import com.comeeatme.web.security.annotation.LoginUsername;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
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
            Pageable pageable, @ModelAttribute PostSearch postSearch, @LoginUsername String username) {
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
            @SortDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            @PathVariable Long memberId, @LoginUsername String username) {
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
    public ResponseEntity<ApiResult<Slice<PostWith<RestaurantPostDto>>>> getListOfRestaurant(
            @SortDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            @PathVariable Long restaurantId, @LoginUsername String username) {
        Long memberId = accountService.getMemberId(username);
        Slice<RestaurantPostDto> posts = postService.getListOfRestaurant(pageable, restaurantId);
        List<Long> postIds = posts.stream()
                .map(RestaurantPostDto::getId)
                .collect(Collectors.toList());
        Set<Long> likedPostIds = getLikedPostIds(memberId, postIds);
        Set<Long> bookmarkedPostIds = getBookmarkedPostIds(memberId, postIds);
        Slice<PostWith<RestaurantPostDto>> postWiths = posts
                .map(post -> PostWith.post(post)
                        .liked(likedPostIds.contains(post.getId()))
                        .bookmarked(bookmarkedPostIds.contains(post.getId()))
                        .build()
                );
        ApiResult<Slice<PostWith<RestaurantPostDto>>> result = ApiResult.success(postWiths);
        return ResponseEntity.ok(result);
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
    public ResponseEntity<ApiResult<PostWith<PostDetailDto>>> get(@PathVariable Long postId, @LoginUsername String username) {
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
            @Valid @RequestBody PostCreate postCreate, @LoginUsername String username) {
        Long memberId = accountService.getMemberId(username);
        if (!imageService.validateImageIds(postCreate.getImageIds(), memberId)) {
            throw new InvalidImageIdception("imageIds=" + postCreate.getImageIds());
        }
        CreateResult<Long> createResult = postService.create(postCreate, memberId);
        ApiResult<CreateResult<Long>> result = ApiResult.success(createResult);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/posts/{postId}")
    public ResponseEntity<ApiResult<UpdateResult<Long>>> patch(
            @Valid @RequestBody PostEdit postEdit, @PathVariable Long postId, @LoginUsername String username) {
        Long memberId = accountService.getMemberId(username);
        if (postService.isNotOwnedByMember(postId, memberId)) {
            throw new EntityAccessDeniedException(String.format("postId=%s, username=%s", postId, username));
        }
        UpdateResult<Long> updateResult = postService.edit(postEdit, postId);
        ApiResult<UpdateResult<Long>> result = ApiResult.success(updateResult);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<ApiResult<DeleteResult<Long>>> delete(
            @PathVariable Long postId, @LoginUsername String username) {
        Long memberId = accountService.getMemberId(username);
        if (postService.isNotOwnedByMember(postId, memberId)) {
            throw new EntityAccessDeniedException(String.format("postId=%s, username=%s", postId, username));
        }
        DeleteResult<Long> deleteResult = postService.delete(postId);
        ApiResult<DeleteResult<Long>> result = ApiResult.success(deleteResult);
        return ResponseEntity.ok(result);
    }

}
