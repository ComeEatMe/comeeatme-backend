package com.comeeatme.batch.service.exception;

public class RequestErrorException extends RuntimeException {

    public RequestErrorException(Throwable cause) {
        super(cause);
    }

    public RequestErrorException(String message) {
        super(message);
    }

    public RequestErrorException(String errorCode, String errorMessage) {
        super("ErrorCode=" + errorCode + ":" + errorMessage);
    }
}
