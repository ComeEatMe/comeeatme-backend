package com.comeeatme.domain.notice;

import com.comeeatme.domain.common.core.EnumMapperType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum NoticeType implements EnumMapperType {

    NOTICE("공지사항"),
    EVENT("이벤트안내"),
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
