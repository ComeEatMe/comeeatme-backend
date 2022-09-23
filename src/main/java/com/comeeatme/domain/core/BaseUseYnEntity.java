package com.comeeatme.domain.core;

import lombok.Getter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@Getter
@MappedSuperclass
public abstract class BaseUseYnEntity {

    @Column(name = "use_yn", nullable = false)
    private Boolean useYn = true;

    public void delete() {
        useYn = false;
    }

    public void restore() {
        useYn = true;
    }
}
