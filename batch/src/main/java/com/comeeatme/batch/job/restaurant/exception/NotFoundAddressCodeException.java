package com.comeeatme.batch.job.restaurant.exception;

public class NotFoundAddressCodeException extends RuntimeException {

    public NotFoundAddressCodeException(String address) {
        super("해당 주소의 법정동 코드를 찾을 수 없습니다. address=" + address);
    }
}
