package com.comeeatme.domain.common.response;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateResult<ID> {

    private ID id;
}
