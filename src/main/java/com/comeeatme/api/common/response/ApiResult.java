package com.comeeatme.api.common.response;

import com.comeeatme.error.dto.ErrorResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResult<T> {

    private boolean success;

    private T data;

    private ErrorResponse error;

    @Builder
    private ApiResult(boolean success, T data, ErrorResponse error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }

    public static <R> ApiResult.ApiResultBuilder<R> success() {
        return ApiResult.<R>builder()
                .success(true);
    }

    public static <R> ApiResult<R> success(R data) {
        return ApiResult.<R>builder()
                .success(true)
                .data(data)
                .build();
    }

    public static ApiResult.ApiResultBuilder<Void> fail() {
        return ApiResult.<Void>builder()
                .success(false);
    }

    public static ApiResult<Void> fail(ErrorResponse error) {
        return ApiResult.<Void>builder()
                .success(false)
                .error(error)
                .build();
    }
}