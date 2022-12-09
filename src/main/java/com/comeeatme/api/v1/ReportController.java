package com.comeeatme.api.v1;

import com.comeeatme.api.common.response.ApiResult;
import com.comeeatme.security.account.service.AccountService;
import com.comeeatme.domain.common.response.CreateResult;
import com.comeeatme.domain.report.request.ReportCreate;
import com.comeeatme.domain.report.service.ReportService;
import com.comeeatme.security.annotation.LoginUsername;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequestMapping("/v1")
@RestController
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    private final AccountService accountService;

    @PostMapping("/posts/{postId}/report")
    public ResponseEntity<ApiResult<CreateResult<Long>>> post(
            @PathVariable Long postId, @Valid @RequestBody ReportCreate reportCreate,
            @LoginUsername String username) {
        Long memberId = accountService.getMemberId(username);
        CreateResult<Long> createResult = reportService.report(postId, reportCreate.getReason(), memberId);
        ApiResult<CreateResult<Long>> result = ApiResult.success(createResult);
        return ResponseEntity.ok(result);
    }
}
