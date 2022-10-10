package com.comeeatme.api.v1;

import com.comeeatme.api.common.dto.ApiResult;
import com.comeeatme.domain.member.request.MemberEdit;
import com.comeeatme.domain.member.service.MemberService;
import com.comeeatme.security.annotation.CurrentUsername;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

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
}
