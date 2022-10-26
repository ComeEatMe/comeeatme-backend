package com.comeeatme.error.exception;

public class AlreadyLikedPostException extends BusinessException {

    public AlreadyLikedPostException(String message) {
        super(ErrorCode.ALREADY_LIKED_POST, message);
    }
}
