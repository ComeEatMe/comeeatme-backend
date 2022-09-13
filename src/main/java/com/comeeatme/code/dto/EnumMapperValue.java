package com.comeeatme.code.dto;


import com.comeeatme.domain.core.EnumMapperType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class EnumMapperValue {

    private final String code;
    private final String title;

    public EnumMapperValue(EnumMapperType enumMapperType) {
        this.code = enumMapperType.getCode();
        this.title = enumMapperType.getTitle();
    }
}
