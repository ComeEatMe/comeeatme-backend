package com.comeeatme.domain.address;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.geo.Point;

import javax.annotation.Nullable;
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

    @Column(name = "location", nullable = false, updatable = false)
    private Point location;

    @Builder
    private Address(
            @Nullable String name,
            @Nullable String roadName,
            Double x,
            Double y) {
        this.name = name;
        this.roadName = roadName;
        this.location = new Point(x, y);
    }
}
