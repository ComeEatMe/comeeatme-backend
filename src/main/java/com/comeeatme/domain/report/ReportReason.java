package com.comeeatme.domain.report;

import com.comeeatme.domain.common.core.EnumMapperType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportReason implements EnumMapperType {

    SPAM("스팸/홍보글입니다."),
    HATE_SPEECH("혐오발언 또는 상징을 포함하고 있습니다."),
    FALSE_INFORMATION("거짓정보를 포함하고 있습니다."),
    SWEAR_WORD("욕설/인신공격을 포함하고 있습니다."),
    DUPLICATE("같은 내용을 도배하고 있습니다."),
    OBSCENE("음란/선정적인 글입니다."),
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
