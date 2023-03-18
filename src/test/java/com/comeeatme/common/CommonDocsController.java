package com.comeeatme.common;

import com.comeeatme.api.common.response.ApiResult;
import com.comeeatme.error.dto.ErrorResponse;
import com.comeeatme.error.exception.ErrorCode;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/docs/common")
public class CommonDocsController {

    @GetMapping("/api-result/success")
    public ResponseEntity<ApiResult<String>> apiResultSuccess() {
        ApiResult<String> result = ApiResult.success("response-data");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/api-result/error")
    public ResponseEntity<ApiResult<Void>> apiResultError() {
        ErrorCode errorCode = ErrorCode.HANDLE_INTERNAL_SERVER_ERROR;
        ErrorResponse errorResponse = ErrorResponse.of(errorCode);
        ApiResult<Void> result = ApiResult.fail(errorResponse);
        HttpStatus status = HttpStatus.valueOf(errorCode.getStatus());
        return ResponseEntity.status(status).body(result);
    }

    @GetMapping("/api-result/field-error")
    public ResponseEntity<ApiResult<Void>> apiResultFieldError() {
        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;
        ErrorResponse errorResponse = ErrorResponse.of(errorCode,
                ErrorResponse.FieldError.of("input-field", "input-value", "error-reason"));
        ApiResult<Void> result = ApiResult.fail(errorResponse);
        HttpStatus status = HttpStatus.valueOf(errorCode.getStatus());
        return ResponseEntity.status(status).body(result);
    }

    @GetMapping("/api-result/slice")
    public ResponseEntity<ApiResult<Slice<String>>> apiResultPage(Pageable pageable) {
        Slice<String> slice = new SliceImpl<>(List.of("content1", "content2"), pageable, true);
        ApiResult<Slice<String>> result = ApiResult.success(slice);
        return ResponseEntity.ok(result);
    }
}
