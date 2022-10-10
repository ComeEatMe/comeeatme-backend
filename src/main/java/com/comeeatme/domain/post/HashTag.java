package com.comeeatme.domain.post;

import com.comeeatme.domain.common.core.EnumMapperType;
import lombok.RequiredArgsConstructor;

import static com.comeeatme.domain.post.HashTagGroup.*;

@RequiredArgsConstructor
public enum HashTag implements EnumMapperType {

    MOODY(MOOD, "감성있는"),
    EATING_ALON(MOOD, "혼밥"),
    GROUP_MEETING(MOOD, "단체모임"),
    DATE(MOOD, "데이트"),
    SPECIAL_DAY(MOOD, "특별한날"),

    FRESH_INGREDIENT(FOOD_PRICE, "신선한재료"),
    SIGNATURE_MENU(FOOD_PRICE, "시그니처메뉴"),
    COST_EFFECTIVENESS(FOOD_PRICE, "가성비"),
    LUXURIOUSNESS(FOOD_PRICE, "고급스러움"),
    STRONG_TASTE(FOOD_PRICE, "자극적인"),

    KINDNESS(CONVENIENCE_ETC, "친절"),
    CLEANLINESS(CONVENIENCE_ETC, "청결"),
    PARKING(CONVENIENCE_ETC, "주차장"),
    PET(CONVENIENCE_ETC, "반려동물"),
    CHILD(CONVENIENCE_ETC, "아이"),
    AROUND_CLOCK(CONVENIENCE_ETC, "24시간"),
    ;

    private final HashTagGroup group;

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
