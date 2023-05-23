package com.comeeatme.api.exception;

public class AlreadyNicknameExistsException extends BusinessException {

    public AlreadyNicknameExistsException(String message) {
        super(ErrorCode.ALREADY_NICKNAME_EXISTS, message);
    }
}
