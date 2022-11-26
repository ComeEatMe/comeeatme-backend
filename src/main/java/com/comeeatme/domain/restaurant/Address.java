package com.comeeatme.domain.restaurant;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;

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
            String roadName,
            Double x,
            Double y) {
        this.name = name;
        this.roadName = roadName;
    }

}
