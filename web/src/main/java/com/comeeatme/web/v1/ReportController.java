package com.comeeatme.web.v1;

import com.comeeatme.api.account.AccountService;
import com.comeeatme.api.common.response.CreateResult;
import com.comeeatme.api.report.ReportService;
import com.comeeatme.api.report.request.ReportCreate;
import com.comeeatme.web.common.response.ApiResult;
import com.comeeatme.web.security.annotation.LoginUsername;
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
