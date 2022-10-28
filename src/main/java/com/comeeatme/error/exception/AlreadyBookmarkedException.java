package com.comeeatme.error.exception;

public class AlreadyBookmarkedException extends BusinessException {

    public AlreadyBookmarkedException(String message) {
        super(ErrorCode.ALREADY_BOOKMARKED, message);
    }
}
