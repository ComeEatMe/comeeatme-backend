package com.comeeatme.api.exception;

public class RequiredAgreementNotAgreeException extends BusinessException {

    public RequiredAgreementNotAgreeException(String message) {
        super(ErrorCode.REQUIRED_AGREEMENT_NOT_AGREE, message);
    }
}
