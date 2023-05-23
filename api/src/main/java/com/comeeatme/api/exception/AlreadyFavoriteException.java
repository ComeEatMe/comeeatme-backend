package com.comeeatme.api.exception;

public class AlreadyFavoriteException extends BusinessException {

    public AlreadyFavoriteException(String message) {
        super(ErrorCode.ALREADY_FAVORITE, message);
    }
}
