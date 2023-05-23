package com.comeeatme.web.security.jwt;

import com.comeeatme.api.exception.ErrorCode;
import com.comeeatme.web.common.response.ApiResult;
import com.comeeatme.web.error.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * JWT Access Token 이 없는 상태로 요청을 보내거나, 토큰 만료, 유효하지 않은 토큰 등을 이용했을 경우 호출
 */
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
        ErrorCode errorCode = ErrorCode.HANDLE_UNAUTHORIZED;
        ErrorResponse errorResponseDto = ErrorResponse.of(errorCode);
        ApiResult<Void> result = ApiResult.<Void>builder()
                .success(false)
                .error(errorResponseDto)
                .build();
        response.setStatus(errorCode.getStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), result);
    }
}
