package com.comeeatme.error.exception;

public class InvalidImageException extends BusinessException {

    public InvalidImageException(String message) {
        super(ErrorCode.INVALID_IMAGE, message);
    }

    public InvalidImageException(Throwable cause) {
        super(ErrorCode.INVALID_IMAGE, cause);
    }
}
