package com.comeeatme.error.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static javax.servlet.http.HttpServletResponse.*;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    HANDLE_INTERNAL_SERVER_ERROR(SC_INTERNAL_SERVER_ERROR, "서버에 문제가 발생했습니다."),
    HANDLE_ACCESS_DENIED(SC_FORBIDDEN, "접근이 허용되지 않습니다."),
    INVALID_INPUT_VALUE(SC_BAD_REQUEST, "유효하지 않은 입력 값입니다."),
    INVALID_TYPE_VALUE(SC_BAD_REQUEST,  "유효하지 않은 타입입니다."),
    ENTITY_NOT_FOUND(SC_NOT_FOUND, "존재하지 않는 자원입니다."),
    ENTITY_ACCESS_DENIED(SC_FORBIDDEN, "접근이 허용되지 않는 리소스입니다."),

    IMAGE_SIZE_EXCEEDED(SC_BAD_REQUEST, "이미지 용량이 초과되었습니다."),
    INVALID_IMAGE(SC_BAD_REQUEST, "잘못된 이미지를 업로드하였습니다."),
    INVALID_IMAGE_ID(SC_BAD_REQUEST, "잘못된 이미지 ID 입니다."),

    ALREADY_LIKED_POST(SC_CONFLICT, "이미 좋아요한 게시물입니다."),
    ALREADY_BOOKMARKED(SC_CONFLICT, "이미 북마크된 게시물입니다."),
    ALREADY_FAVORITE(SC_CONFLICT, "이미지 맛집 등록한 음식점입니다."),
    ;

    private final int status;

    private final String message;
}
