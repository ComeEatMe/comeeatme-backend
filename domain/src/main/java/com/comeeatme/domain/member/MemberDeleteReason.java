package com.comeeatme.domain.member;

import com.comeeatme.domain.common.core.EnumMapperType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum MemberDeleteReason implements EnumMapperType {
    FREQUENT_ERROR("앱에서 오류나 장애가 자주 발생해요"),
    NO_INFORMATION("찾는 정보가 없어요"),
    DELETE_PERSONAL_INFORMATION("개인정보를 삭제하고 싶어요"),
    LOW_VISIT_FREQUENCY("방문 빈도가 낮아요"),
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
