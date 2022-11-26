package com.comeeatme.domain.address;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address {

    @Column(name = "address_name", nullable = false, updatable = false)
    private String name;

    @Column(name = "road_address_name", nullable = false, updatable = false)
    private String roadName;

    @Builder
    private Address(
            String name,
            String roadName) {
        this.name = name;
        this.roadName = roadName;
    }

}
