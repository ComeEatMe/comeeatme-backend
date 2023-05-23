package com.comeeatme.batch.restaurant.exception;

import com.comeeatme.batch.restaurant.LocalDataRestaurantDto;

public class NoAddressException extends RuntimeException {

    public NoAddressException(LocalDataRestaurantDto item) {
        super("주소가 존재하지 않습니다. " + item);
    }
}
