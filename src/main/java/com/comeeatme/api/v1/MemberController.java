package com.comeeatme.api.v1;

import com.comeeatme.api.common.dto.ApiResult;
import com.comeeatme.domain.common.response.DuplicateResult;
import com.comeeatme.domain.member.request.MemberEdit;
import com.comeeatme.domain.member.request.MemberSearch;
import com.comeeatme.domain.member.response.MemberSimpleDto;
import com.comeeatme.domain.member.service.MemberService;
import com.comeeatme.security.annotation.CurrentUsername;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@RequestMapping("/v1/members")
@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PatchMapping
    public ResponseEntity<ApiResult<Long>> patch(
            @Valid @RequestBody MemberEdit memberEdit, @CurrentUsername String username) {
        Long memberId = memberService.edit(memberEdit, username);
        ApiResult<Long> result = ApiResult.success(memberId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/duplicate/nickname")
    public ResponseEntity<ApiResult<DuplicateResult>> getNicknameDuplicate(
            @RequestParam @NotBlank @Size(max = 15) String nickname) {
        DuplicateResult duplicateResult = memberService.checkNicknameDuplicate(nickname);
        ApiResult<DuplicateResult> result = ApiResult.success(duplicateResult);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<ApiResult<Slice<MemberSimpleDto>>> getList(
            Pageable pageable, @ModelAttribute MemberSearch memberSearch) {
        Slice<MemberSimpleDto> simpleDtos = memberService.search(pageable, memberSearch);
        ApiResult<Slice<MemberSimpleDto>> result = ApiResult.success(simpleDtos);
        return ResponseEntity.ok(result);
    }
}
