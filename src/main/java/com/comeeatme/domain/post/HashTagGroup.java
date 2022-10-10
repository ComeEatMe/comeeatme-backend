package com.comeeatme.domain.post;

import com.comeeatme.domain.common.core.EnumMapperType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum HashTagGroup implements EnumMapperType {

    MOOD("분위기"),
    FOOD_PRICE("음식/가격"),
    CONVENIENCE_ETC("편의시설/기타")
    ;

    private final String title;

    @Override
    public String getCode() {
        return name();
    }

    @Override
    public String getTitle() {
        return title;
    }
}
