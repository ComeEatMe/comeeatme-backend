package com.comeeatme.web.security;

import com.comeeatme.api.exception.ErrorCode;
import com.comeeatme.web.common.response.ApiResult;
import com.comeeatme.web.error.dto.ErrorResponse;
import com.comeeatme.web.security.response.LoginResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/docs/auth")
@RestController
public class AuthDocsController {

    @GetMapping("/login-response")
    public ResponseEntity<LoginResponse> loginResponse() {
        LoginResponse loginResponse = LoginResponse.builder()
                .memberId(1L)
                .accessToken("<Access token>")
                .refreshToken("<Refresh token>")
                .build();
        return ResponseEntity.ok(loginResponse);
    }

    @GetMapping("/unauthorized-response")
    public ResponseEntity<ApiResult<Void>> unauthorizedResponse() {
        ErrorCode errorCode = ErrorCode.HANDLE_UNAUTHORIZED;
        ErrorResponse errorResponseDto = ErrorResponse.of(errorCode);
        ApiResult<Void> result = ApiResult.<Void>builder()
                .success(false)
                .error(errorResponseDto)
                .build();
        return ResponseEntity.status(errorCode.getStatus()).body(result);
    }
}
