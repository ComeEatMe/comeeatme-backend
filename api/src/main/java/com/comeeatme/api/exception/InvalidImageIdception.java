package com.comeeatme.api.exception;

public class InvalidImageIdception extends BusinessException {

    public InvalidImageIdception(String message) {
        super(ErrorCode.INVALID_IMAGE_ID, message);
    }

}
