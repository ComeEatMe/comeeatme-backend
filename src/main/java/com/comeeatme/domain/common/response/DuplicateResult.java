package com.comeeatme.domain.common.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DuplicateResult {

    boolean duplicate;

    @Builder
    public DuplicateResult(boolean duplicate) {
        this.duplicate = duplicate;
    }
}
