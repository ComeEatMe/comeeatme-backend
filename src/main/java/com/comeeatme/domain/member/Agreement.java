package com.comeeatme.domain.member;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Agreement {

    TERMS_OF_SERVICE("이용약관", true, "/agreement/terms-of-service.html"),
    PERSONAL_INFORMATION("개인정보 수집 및 이용 동의", true, "/agreement/terms-of-service.html"),
    ;

    private final String title;
    private final boolean required;
    private final String link;
}
