package com.comeeatme.api.address.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AddressCodeDto {

    private String code;

    private String name;

    private Boolean terminal;

    @Builder
    private AddressCodeDto(String code, String name, Boolean terminal) {
        this.code = code;
        this.name = name;
        this.terminal = terminal;
    }

}
