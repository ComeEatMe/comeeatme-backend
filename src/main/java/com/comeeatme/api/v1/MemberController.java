package com.comeeatme.api.v1;

import com.comeeatme.api.common.response.ApiResult;
import com.comeeatme.security.account.service.AccountService;
import com.comeeatme.domain.common.response.DeleteResult;
import com.comeeatme.domain.common.response.DuplicateResult;
import com.comeeatme.domain.common.response.UpdateResult;
import com.comeeatme.domain.image.service.ImageService;
import com.comeeatme.domain.member.request.MemberEdit;
import com.comeeatme.domain.member.request.MemberImageEdit;
import com.comeeatme.domain.member.request.MemberSearch;
import com.comeeatme.domain.member.response.MemberDetailDto;
import com.comeeatme.domain.member.response.MemberSimpleDto;
import com.comeeatme.domain.member.service.MemberService;
import com.comeeatme.error.exception.EntityAccessDeniedException;
import com.comeeatme.security.annotation.CurrentUsername;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@RequestMapping("/v1")
@RestController
@RequiredArgsConstructor
public class MemberController {

    private final AccountService accountService;

    private final MemberService memberService;

    private final ImageService imageService;

    @PatchMapping("/member")
    public ResponseEntity<ApiResult<UpdateResult<Long>>> patch(
            @Valid @RequestBody MemberEdit memberEdit, @CurrentUsername String username) {
        Long memberId = accountService.getMemberId(username);
        UpdateResult<Long> updateResult = memberService.edit(memberEdit, memberId);
        ApiResult<UpdateResult<Long>> result = ApiResult.success(updateResult);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/member/image")
    public ResponseEntity<ApiResult<UpdateResult<Long>>> patchImage(
            @RequestBody @Valid MemberImageEdit memberImageEdit, @CurrentUsername String username) {
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
    public ResponseEntity<ApiResult<DeleteResult<Long>>> deleteImage(@CurrentUsername String username) {
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
}
