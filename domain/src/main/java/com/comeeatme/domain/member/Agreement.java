package com.comeeatme.domain.member;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Agreement {

    TERMS_OF_SERVICE("이용약관", true, "/agreement/terms-of-service.html"),
    ;

    private final String title;
    private final boolean required;
    private final String link;
}
