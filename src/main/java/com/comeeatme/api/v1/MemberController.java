package com.comeeatme.api.v1;

import com.comeeatme.api.common.response.ApiResult;
import com.comeeatme.domain.account.service.AccountService;
import com.comeeatme.domain.bookmark.service.BookmarkService;
import com.comeeatme.domain.comment.service.CommentService;
import com.comeeatme.domain.common.response.CreateResult;
import com.comeeatme.domain.common.response.DeleteResult;
import com.comeeatme.domain.common.response.DuplicateResult;
import com.comeeatme.domain.common.response.UpdateResult;
import com.comeeatme.domain.favorite.service.FavoriteService;
import com.comeeatme.domain.image.service.ImageService;
import com.comeeatme.domain.like.service.LikeService;
import com.comeeatme.domain.member.Agreement;
import com.comeeatme.domain.member.request.*;
import com.comeeatme.domain.member.response.MemberAgreements;
import com.comeeatme.domain.member.response.MemberDetailDto;
import com.comeeatme.domain.member.response.MemberSimpleDto;
import com.comeeatme.domain.member.service.MemberNicknameCreator;
import com.comeeatme.domain.member.service.MemberService;
import com.comeeatme.domain.post.service.PostService;
import com.comeeatme.error.exception.EntityAccessDeniedException;
import com.comeeatme.error.exception.RequiredAgreementNotAgreeException;
import com.comeeatme.security.annotation.LoginUsername;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("/v1")
@RestController
@RequiredArgsConstructor
public class MemberController {

    private final AccountService accountService;

    private final MemberService memberService;

    private final ImageService imageService;

    private final MemberNicknameCreator memberNicknameCreator;

    private final PostService postService;

    private final CommentService commentService;

    private final FavoriteService favoriteService;

    private final BookmarkService bookmarkService;

    private final LikeService likeService;

    @PatchMapping("/member")
    public ResponseEntity<ApiResult<UpdateResult<Long>>> patch(
            @Valid @RequestBody MemberEdit memberEdit, @LoginUsername String username) {
        Long memberId = accountService.getMemberId(username);
        UpdateResult<Long> updateResult = memberService.edit(memberEdit, memberId);
        ApiResult<UpdateResult<Long>> result = ApiResult.success(updateResult);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/member/image")
    public ResponseEntity<ApiResult<UpdateResult<Long>>> patchImage(
            @RequestBody @Valid MemberImageEdit memberImageEdit, @LoginUsername String username) {
        Long memberId = accountService.getMemberId(username);
        Long imageId = memberImageEdit.getImageId();
        if (imageService.isNotOwnedByMember(memberId, imageId)) {
            throw new EntityAccessDeniedException(String.format(
                    "Member.id=%s, Image.id=%s", memberId, imageId));
        }
        UpdateResult<Long> updateResult = memberService.editImage(memberId, imageId);
        ApiResult<UpdateResult<Long>> result = ApiResult.success(updateResult);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/member/image")
    public ResponseEntity<ApiResult<DeleteResult<Long>>> deleteImage(@LoginUsername String username) {
        Long memberId = accountService.getMemberId(username);
        DeleteResult<Long> deleteResult = memberService.deleteImage(memberId);
        ApiResult<DeleteResult<Long>> result = ApiResult.success(deleteResult);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/members/duplicate/nickname")
    public ResponseEntity<ApiResult<DuplicateResult>> getNicknameDuplicate(
            @RequestParam @NotBlank @Size(max = 15) String nickname) {
        DuplicateResult duplicateResult = memberService.checkNicknameDuplicate(nickname);
        ApiResult<DuplicateResult> result = ApiResult.success(duplicateResult);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/members")
    public ResponseEntity<ApiResult<Slice<MemberSimpleDto>>> getList(
            Pageable pageable, @ModelAttribute MemberSearch memberSearch) {
        Slice<MemberSimpleDto> simpleDtos = memberService.search(pageable, memberSearch);
        ApiResult<Slice<MemberSimpleDto>> result = ApiResult.success(simpleDtos);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/members/{memberId}")
    public ResponseEntity<ApiResult<MemberDetailDto>> get(@PathVariable Long memberId) {
        MemberDetailDto memberDetailDto = memberService.get(memberId);
        ApiResult<MemberDetailDto> result = ApiResult.success(memberDetailDto);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/signup")
    public ResponseEntity<ApiResult<MemberAgreements>> getSignupAgreements() {
        MemberAgreements memberAgreements = MemberAgreements.create();
        ApiResult<MemberAgreements> apiResult = ApiResult.success(memberAgreements);
        return ResponseEntity.ok(apiResult);
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResult<CreateResult<Long>>> signup(
            @Valid @RequestBody MemberSignup memberSignup, @LoginUsername String username) {
        String nickname = memberNicknameCreator.create();
        while (memberService.checkNicknameDuplicate(nickname).isDuplicate()) {
            nickname = memberNicknameCreator.create();
        }
        List<Agreement> notAgreedRequired = Arrays.stream(Agreement.values())
                .filter(Agreement::isRequired)
                .filter(agreement -> !memberSignup.getAgreeOrNot().getOrDefault(agreement, false))
                .collect(Collectors.toList());
        if (!notAgreedRequired.isEmpty()) {
            throw new RequiredAgreementNotAgreeException(notAgreedRequired.toString());
        }
        CreateResult<Long> memberCreateResult = memberService.create(nickname);
        accountService.signupMember(username, memberCreateResult.getId());
        ApiResult<CreateResult<Long>> apiResult = ApiResult.success(memberCreateResult);
        return ResponseEntity.ok(apiResult);
    }

    @DeleteMapping("/member")
    public ResponseEntity<ApiResult<DeleteResult<Long>>> delete(@LoginUsername String username) {
        Long memberId = accountService.getMemberId(username);

        postService.deleteAllOfMember(memberId);
        imageService.deleteAllOfMember(memberId);
        commentService.deleteAllOfMember(memberId);
        favoriteService.deleteAllOfMember(memberId);
        bookmarkService.deleteAllOfMember(memberId);
        likeService.deleteAllOfMember(memberId);
        DeleteResult<Long> deleteResult = memberService.delete(memberId);
        accountService.delete(username);

        ApiResult<DeleteResult<Long>> apiResult = ApiResult.success(deleteResult);
        return ResponseEntity.ok(apiResult);
    }

    @PostMapping("/member/delete-reason")
    public ResponseEntity<ApiResult<Void>> postDeleteReason(
            @LoginUsername String username, @RequestBody @Valid MemberDelete memberDelete) {
        Long memberId = accountService.getMemberId(username);
        memberService.registerDeleteReason(memberId, memberDelete.getReason());
        return ResponseEntity.ok(ApiResult.success());
    }

}
