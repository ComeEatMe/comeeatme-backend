package com.comeeatme.domain.address;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.geo.Point;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address {

    @Column(name = "address_name", nullable = false)
    private String name;

    @Column(name = "road_address_name", nullable = false)
    private String roadName;

    @Column(name = "point", nullable = false)
    private Point point;

    @Builder
    private Address(
            String name,
            String roadName,
            Double x,
            Double y) {
        this.name = name;
        this.roadName = roadName;
        this.point = new Point(x, y);
    }
}
